package application;

import static common.Log.COMM;
import static common.Log.CTRL;

import java.io.IOException;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;


import course.Coureur;
import course.Plateau;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;

import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import messages.EtatPlateau;
import messages.Message;
import messages.ProtocoleMessageType;
import reseau.Client;

public class PlateauViewCtrl implements javafx.fxml.Initializable {
	//############  partie des attributs décrits dans le fxml généré par SceneBuilder
	@FXML
	private BorderPane panel;

	@FXML
	private HBox hboxPlateau;

	@FXML
	private HBox bottomBox;

	@FXML
	private TextArea texteConsole;


	//############  partie des attributs ajoutés à la main

	

	/**
	 * Canvas dans laquelle on va dessiner le centre le page.
	 */
	private Canvas canvasPlateau;
	/**
	 * Contexte graphique placé dans le canvasPlateau.
	 * Il va servire à dessiner l'image du plateau.
	 */
	private GraphicsContext  gcPlateau;

	/**
	 * Pane qui gère la partie basse de l'interface graphique.
	 */
	private AnchorPane bottomPanel;
	
	/**
	 * Canvas dans laquelle on va dessiner le bas de page.
	 */
	private Canvas canvasBottom;
	/**
	 * Contexte graphique placé dans le canvasPlateau.
	 * Il va servire à dessiner l'image du bas de page.
	 */
	private GraphicsContext  gcBottom;


	/**
	 * Bouton qui gère toutes les interactions avec le joueur.
	 * Il n'est affiché et actif ue si le joueur doit agir, et
	 * son contenu change suivant la phase de jeu.
	 */
	private Button boutonJoueur;
	
	/**
	 * Le dernier message de protocole reçu.
	 * Utilisé pour connaître l'état du jeu.^
	 */
	private ProtocoleMessageType lastMessageType;
	
	
	/**
	 * référence sur l'instance des data contruite par le premier contrôleur.
	 * (patron de conception du singleton).
	 * Permet de retrouver la référence du Client connecté au serveur (getConnexion()),
	 * et le numéro du joueur dans l'odre de jeu (getMonNumero()).
	 */
	private DataCtrl dataCtrl;

	
	/**
	 * Service pour gérer la réception de messages TCP.
	 */
	private Service<Message> receptionService;

	/**
	 * Initialisation du contrôleur, appelée automatiquement par JavaFX après
	 * le chargement du FXML.
	 * 
	 * - Retrouve les données initialisées par le premier contrôleur (patron
	 * de conceotion du singleton) sauvées dans la classe DataCtrl.
	 * - Démarre le service de réception des messages TCP.
	 * 
	 * @param location URL de localisation du fichier FXML
	 * @param resources Ressources externes
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {

		try {
			texteConsole.setEditable(false);
			// utilisation de la méthode de classe pour retrouver 
			// la référence sur l'instance de DataCtrl déjà créée.
			dataCtrl = DataCtrl.getInstance();

			// mise en place du service de réception des messages
			initServiceReception();
			receptionService.start();

			// rien d'autre à ce moment du jeu: on attend la réception du
			// messages INIT_PLATEAU pour avoir les informations nécessaires
			// à la mise en place des Canvas et du contextes graphique.


		} catch (Exception e) {
			// TODO
		}

	}


	//#################   Partie pour la gestion des réceptions #################
	/**
	 * Service lançant la tâche de réception.
	 */
	private void initServiceReception() {
		// service de réception
		receptionService = new Service<Message>(){

			@Override
			protected Task<Message> createTask() {

				return new Task<Message>(){

					@Override
					protected Message call() throws Exception {
						// cette lecture est un appel bloquant. Il faut donc le placer
						// dans un service pour éviter de figer l'interface graphique.
						return dataCtrl.getConnexion().lireMessage();
					}
				};

			}
		};

		// Gestionaire pour la réussite de la récéption d'un message .
		EventHandler<WorkerStateEvent> succeedEvent = new EventHandler<WorkerStateEvent> () {
			@Override
			public void handle(WorkerStateEvent event) {
				// la lecture d'un message s'est terminée. On le gére.


				Message message = receptionService.getValue();

				COMM.trace("ConnexionCtrl::receptionService::succeedEvent::handle: message reçu = " + message);

				gererLesMessages(message);
				
				// on laisse la gestion du message décider de relancer ou non le service de réception

			}
		};

		// Gestionaire pour l'échec de la réception d'un message
		EventHandler<WorkerStateEvent> failedEvent = new EventHandler<WorkerStateEvent> () {
			@Override
			public void handle(WorkerStateEvent event) {
				// TODO Gestion des décos

			}
		};


		// mise en place des 2 callbacks (réussite et échec)
		receptionService.setOnSucceeded(succeedEvent);
		receptionService.setOnFailed(failedEvent);

	}



	//#################   Traitement des messages reçus #################
	/**
	 * Gestion des messages reçu par le client.
	 * À ce niveau du protocole (phase de jeu) le client
	 * attend trois types messages:
	 *  - NOUVEAU_TOUR: annonce le début d'un nouveau tour (et donne
	 *  l'état du plateau avec le numéro du joueur actif). 
	 *  - NOTIFICATION_VALEUR_DES: annonce la valeur des dés lancés
	 *  par le joueur actif.
	 *  - NOTIFICATION_FIN: annonce la fin de la partie (et donne
	 *  l'état du plateau avec le classement des joueurs). 
	 * @param message
	 *        le message reçu.
	 */
	public void gererLesMessages(final Message message) {
		CTRL.trace("CourseViewCtrl::gestionDesMessages: " + message);
		if (message == null) {
			// TODO Gestion des déco
			return;
		}

		lastMessageType = message.getProtocoleMessage();
		switch (message.getProtocoleMessage()) {
		case NOUVEAU_TOUR: {
			if (canvasPlateau == null) {
				// premier tour de jeu, on crée le canvas du plateau 
				// grâce aux informations contenues dans ce premier message.^
				gererInitPlateau(message);
			}
			gererNouveauTour(message);
			break;
		}

		case NOTIFICATION_VALEUR_DES: {
			gererValeurDes(message);
			break;
		}
		
		case NOTIFICATION_FIN: {
			gererFin(message);
			return; // on ne relance pas le service de réception, le serveur est parti (et déconecté!)
		}

		}

		// on relance le service de réception pour recevoir les autres messages
		receptionService.restart();
	}

	/**
	 * Gestion du premier message NOUVEAU_TOUR.
	 * Mise en place des Canvas et des contexte graphique dimensionnés pour le plateau.
	 * @param message
	 *        le message de type NOUVEAU_TOUR qui contient le Plateau avec les
	 *        informations pour dimensionner les canvas. 
	 */
	private void gererInitPlateau(final Message message) {
		texteConsole.setPrefWidth(ConfigVue.CONSOLE_LARGEUR);
		EtatPlateau etatPlateau = (EtatPlateau) message.getData();
		initCanvas(etatPlateau);

		retaillerFenetre(etatPlateau);
	}


	/**
	 * Gestion du message NOUVEAU_TOUR.
	 * @param message
	 *        le message de type NOUVEAU_TOUR qui contient le Plateau avec les
	 *        informations pour afficher les informations de la console
	 *        et dessiner le plateau.
	 */
	private void gererNouveauTour(final Message message) {
		EtatPlateau etatPlateau = (EtatPlateau) message.getData();
		Plateau plateau = etatPlateau.getPlateau();

		// la console
		texteConsole.clear();
		int numeroCoureurActif = plateau.getNumCoureurActif();
		boolean actif = numeroCoureurActif == dataCtrl.getMonNumero();
		String nomCoureurActif = plateau.getCoureurs().get(numeroCoureurActif).getPseudo();
		texteConsole.clear();
		texteConsole.appendText("Tour de jeu de " + nomCoureurActif + "\n");
		afficherListeCoureurs(etatPlateau);
		texteConsole.appendText(nomCoureurActif + " doit lancer les dés\n");

		// le plateau
		afficherPlateau(etatPlateau);

		// la partie basse
		dessinerDes(null);
		dessinerBoutonJoueur(actif, ConfigVue.LANCER_DE);
	}


	/**
	 * Gestion du message NOTIFICATION_VALEUR_DES.
	 * @param message
	 *        le message de type NOTIFICATION_VALEUR_DES qui contient le Plateau 
	 *        avec la valeur des dés à afficher.
	 */
	private void gererValeurDes(final Message message) {
		EtatPlateau etatPlateau = (EtatPlateau) message.getData();
		Plateau plateau = etatPlateau.getPlateau();

		// la console
		texteConsole.clear();
		int numeroCoureurActif = plateau.getNumCoureurActif();
		boolean actif = numeroCoureurActif == dataCtrl.getMonNumero();
		String nomCoureurActif = plateau.getCoureurs().get(numeroCoureurActif).getPseudo();
		texteConsole.clear();
		texteConsole.appendText("Tour de jeu de " + nomCoureurActif + "\n");
		afficherListeCoureurs(etatPlateau);
		texteConsole.appendText(nomCoureurActif + " doit avancer son pion\n");

		// le plateau
		afficherPlateau(etatPlateau);

		// la partie basse
		dessinerDes(plateau);
		dessinerBoutonJoueur(actif, ConfigVue.AVANCER_PION);


	}

	/**
	 * Gestion du message NOTIFICATION_FIN.
	 * @param message
	 *        le message de type NOTIFICATION_FIN qui contient le Plateau 
	 *        dans son état final.
	 */
	private void gererFin(final Message message) {
		EtatPlateau etatPlateau = (EtatPlateau) message.getData();

		// la console
		texteConsole.clear();
		texteConsole.clear();
		texteConsole.appendText("La partie est finie\n");
		afficherListeCoureurs(etatPlateau);

		// le plateau
		afficherPlateau(etatPlateau);

		// la partie basse
		dessinerDes(null);
		dessinerBoutonJoueur(true, ConfigVue.FERMER_JEU);
	}

	

	//#################   Initialisation des canvas et contextes graphique #################

	/**
	 * Met en place les canvas et les contextes graphiques mis à la taille pour le plateau
	 * choisis.
	 * @param etatPlateau
	 */
	private void initCanvas(final EtatPlateau etatPlateau) {
		int nbCases = etatPlateau.getPlateau().getNbCasesPlateau();
		int nbBranches = (nbCases / ConfigVue.NB_CASES_BRANCHE) + 1;
		int hauteur = 2 * ConfigVue.MARGE_HAUTEUR + 2 * ConfigVue.TAILLE_CASES * nbBranches;
		int largeur = 2 * ConfigVue.MARGE_LARGEUR + ConfigVue.TAILLE_CASES * ConfigVue.NB_CASES_LIGNE;


		// Mise en place de la partie console
		texteConsole.setMinWidth(ConfigVue.CONSOLE_LARGEUR);
		texteConsole.setMinHeight(hauteur);


		// Canvas partie centrale (le dessin du plateau)
		CTRL.trace("CourseViewCtrl::initCanvas hauteur =" + hauteur + ".");

		canvasPlateau = new Canvas(largeur, hauteur);
		// pour que les events claviers soient visibles par le canvas
		// canvas.setFocusTraversable(true); pas besoin pour ce cas
		gcPlateau = canvasPlateau.getGraphicsContext2D();

		hboxPlateau.getChildren().add(canvasPlateau);


		// Canvas partie basse (les dés, et les interactions avec les joueurs)
		bottomPanel = new AnchorPane();
		bottomPanel.setMinWidth(ConfigVue.MARGE_LARGEUR  + ConfigVue.LARGEUR_BRANCHE + ConfigVue.MARGE_LARGEUR * 2);
		bottomPanel.setMinHeight(ConfigVue.BOTTOM_HAUTEUR);
		bottomBox.setMinWidth(ConfigVue.MARGE_LARGEUR  + ConfigVue.LARGEUR_BRANCHE + ConfigVue.MARGE_LARGEUR * 2);
		bottomBox.setMinHeight(ConfigVue.BOTTOM_HAUTEUR);
		bottomBox.getChildren().add(bottomPanel);


		int hauteur2 = ConfigVue.BOTTOM_HAUTEUR;
		int largeur2 = ConfigVue.LEFT_LARGEUR + largeur / 2;
		canvasBottom = new Canvas(largeur2, hauteur2);
		gcBottom = canvasBottom.getGraphicsContext2D();


		// le bouton d'interaction avec le joueur
		boutonJoueur = new Button();
		boutonJoueur.setOnMouseClicked( (event) -> onBoutonJoueur(event));
		AnchorPane.setRightAnchor(boutonJoueur, canvasBottom.getWidth() + ConfigVue.MARGE_DE ); 
		AnchorPane.setTopAnchor(boutonJoueur, (double)ConfigVue.MARGE_HAUTEUR_BOTTOM); 
		bottomPanel.getChildren().add(canvasBottom);
		bottomPanel.getChildren().add(boutonJoueur);
		// désactive et cache le bouton: seul le joueur actif aura ce bouton
		boutonJoueur.setManaged(false); 
		boutonJoueur.setVisible(false);


		// pour débug
//		gcBottom.setFont(Font.font("Helvetica", FontWeight.LIGHT, 8));
//		gcBottom.setFill(Color.BLACK);
//		gcBottom.setStroke(Color.BLACK);
//
//		for(int posY = 20; posY < ConfigVue.BOTTOM_HAUTEUR; posY += 20) {
//			gcBottom.fillText(posY + "", 20, posY); 
//		}


	}

	/**
	 * Retaille la fenètre de l'interface pour l'adapter au plateau.
	 * @param etatPlateau
	 *        contient les informations pour connaître la taille de la fenêtre.
	 *        
	 */
	private void retaillerFenetre(final EtatPlateau etatPlateau) {
		Scene scene = panel.getScene();
		int nbBranches = etatPlateau.getPlateau().getNbCasesPlateau() / ConfigVue.NB_CASES_BRANCHE + 1;
		int plateauHauteur = ConfigVue.MARGE_HAUTEUR * 2 +
				nbBranches * ConfigVue.TAILLE_CASES * 2 +
				ConfigVue.BOTTOM_HAUTEUR + ConfigVue.DECORATION_HAUTEUR;
		CTRL.trace("CourseViewCtrl::retaillerFenetre plateauHueteur=" + plateauHauteur + ".");
		scene.getWindow().setHeight(plateauHauteur);
		scene.getWindow().setWidth(ConfigVue.PLATEAU_LARGEUR + ConfigVue.LEFT_LARGEUR);
	}


	//#################   Dessin des divers éléments  #################
	/**
	 * Affiche ou cache le bouton d'actions du joueur.
	 * @param actif
	 *            true si le bouton doit être affiché et géré, et false sinon.
	 * @param texte
	 *            le texte du bouton.
	 */
	private void dessinerBoutonJoueur(final boolean actif, final String texte) {
		CTRL.trace("CourseViewCtrl::dessinerBoutonJoueur(" + actif + ", " + texte + ")");
		boutonJoueur.setManaged(actif); 
		boutonJoueur.setVisible(actif);
		boutonJoueur.setText(texte);
	}

	/**
	 * Dessine les dès sur le partie bottom de l'interface.
	 * @param plateau
	 *        le plateau qui contient la valeur des dés.
	 */
	private void dessinerDes(final Plateau plateau) {
		CTRL.trace("CourseViewCtrl::dessinerDes. Plateau = " + plateau);
		boolean epuise = false;
		int de1 = 0;
		int de2 = 0;
		if (plateau != null) {
			epuise = plateau.getCoureurs().get(plateau.getNumCoureurActif()).estEpuise();
			de1 = plateau.getDe1();
			de2 = plateau.getDe2();
		}

		gcBottom.clearRect(0, 0, canvasBottom.getWidth(), canvasBottom.getHeight());
		gcBottom.setFont(Font.font("Helvetica", FontWeight.BOLD, ConfigVue.TAILLE_POLICE_DE));
		gcBottom.setFill(Color.BLACK);
		gcBottom.setStroke(Color.BLACK);


		String nomImageDe1 = String.format(ConfigVue.FORMAT_IMAGES_DE, de1);
		String urlImageDe1 = getClass().getResource(ConfigVue.IMAGES_DIR + "/" + nomImageDe1).toString();
		CTRL.trace("CourseViewCtrl::initCanvas urlImage =" + urlImageDe1 + ".");
		Image imageDe1 = new Image(urlImageDe1, ConfigVue.TAILLE_DE, ConfigVue.TAILLE_DE, false, false);
		gcBottom.drawImage(imageDe1, ConfigVue.MARGE_BOTTON, ConfigVue.MARGE_HAUTEUR_BOTTOM);
		String texteDe1 =  String.format(ConfigVue.FORMAT_TEXTE_DE, 1);
		gcBottom.fillText(texteDe1, ConfigVue.MARGE_BOTTON + ConfigVue.MARGE_TEXTE_DE_LARGEUR, 
				ConfigVue.MARGE_HAUTEUR_BOTTOM + ConfigVue.TAILLE_DE + ConfigVue.MARGE_TEXTE_DE_HAUTEUR );


		String nomImageDe2 = String.format(ConfigVue.FORMAT_IMAGES_DE, de2);
		String urlImageDe2 = getClass().getResource(ConfigVue.IMAGES_DIR + "/" + nomImageDe2).toString();
		CTRL.trace("CourseViewCtrl::initCanvas urlImage =" + urlImageDe2 + ".");
		Image imageDe2 = new Image(urlImageDe2, ConfigVue.TAILLE_DE, ConfigVue.TAILLE_DE, false, false);
		gcBottom.drawImage(imageDe2, ConfigVue.MARGE_BOTTON + ConfigVue.TAILLE_DE + ConfigVue.MARGE_DE,
				ConfigVue.MARGE_HAUTEUR_BOTTOM);
		String texteDe2 =  String.format(ConfigVue.FORMAT_TEXTE_DE, 2);
		gcBottom.fillText(texteDe2, ConfigVue.MARGE_BOTTON + ConfigVue.TAILLE_DE + ConfigVue.MARGE_DE+ ConfigVue.MARGE_TEXTE_DE_LARGEUR, 
				ConfigVue.MARGE_HAUTEUR_BOTTOM + ConfigVue.TAILLE_DE + ConfigVue.MARGE_TEXTE_DE_HAUTEUR );

		if (epuise) {
			gcBottom.setStroke(Color.BLACK);
			gcBottom.setLineWidth(4);
			gcBottom.strokeLine(ConfigVue.MARGE_BOTTON + ConfigVue.TAILLE_DE + ConfigVue.MARGE_DE,
					ConfigVue.MARGE_HAUTEUR_BOTTOM,
					ConfigVue.MARGE_BOTTON + 2 * ConfigVue.TAILLE_DE + ConfigVue.MARGE_DE, 
					ConfigVue.MARGE_HAUTEUR_BOTTOM  + ConfigVue.TAILLE_DE);
			gcBottom.strokeLine(ConfigVue.MARGE_BOTTON + 2 * ConfigVue.TAILLE_DE + ConfigVue.MARGE_DE,
					ConfigVue.MARGE_HAUTEUR_BOTTOM,
					ConfigVue.MARGE_BOTTON + ConfigVue.TAILLE_DE + ConfigVue.MARGE_DE,
					ConfigVue.MARGE_HAUTEUR_BOTTOM  + ConfigVue.TAILLE_DE);
		}

	}



	/**
	 * Calcul la position sur l'axe horizontal de l'angle haut gauche de la case
	 * num sur le canvas du plateau.
	 * @param num
	 *       numéro de case (la numérotation commence à 1).
	 * @return
	 *       la valeur sur l'axe horizontal de l'angle haut gauche de la case num.
	 */
	private int calculerPosXCase(final int num) {
		int posX;
		// on décale la numérotation pour commencer à 0.
		int num0 = num - 1;
		int numBranche = num0 / ConfigVue.NB_CASES_BRANCHE;
		int posDansBranche = num0 % ConfigVue.NB_CASES_BRANCHE;


		if (numBranche % 2 == 0) {
			// les branches qui vont de gauche à droite
			posX = ConfigVue.MARGE_LARGEUR;
			if (posDansBranche >= ConfigVue.NB_CASES_LIGNE) {
				// les cases après le coude
				posX += ConfigVue.LARGEUR_BRANCHE - ConfigVue.TAILLE_CASES;
			}
			else {
				posX += ConfigVue.TAILLE_CASES * posDansBranche;
			}
		} else {
			// les branches qui vont de droite à gauche
			posX = ConfigVue.MARGE_LARGEUR + ConfigVue.LARGEUR_BRANCHE - ConfigVue.TAILLE_CASES;
			if (posDansBranche >= ConfigVue.NB_CASES_LIGNE) {
				// les cases après le coude
				posX = ConfigVue.MARGE_LARGEUR;
			} 
			else {
				posX -= ConfigVue.TAILLE_CASES * posDansBranche;
			}

		}
		return posX;
	}

	/**
	 * Calcul la position sur l'axe vertical de l'angle haut gauche de la case
	 * num sur le canvas du plateau.
	 * @param num
	 *       numéro de case (la numérotation commence à 1).
	 * @return
	 *       la valeur sur l'axe vertical de l'angle haut gauche de la case num.
	 */
	private int calculerPosYCase(final int num) {
		// pour le calcul, on décale les cases pour commencer la numérotation à 0.
		int num0 = num - 1;
		int  posY = ConfigVue.MARGE_HAUTEUR;

		int numBranche = num0 / ConfigVue.NB_CASES_BRANCHE;
		posY += numBranche + ConfigVue.HAUTEUR_BRANCHE * numBranche;
		int posDansBranche = num0 % ConfigVue.NB_CASES_BRANCHE;
		if (posDansBranche >= ConfigVue.NB_CASES_LIGNE) {
			posY += (posDansBranche - ConfigVue.NB_CASES_LIGNE + 1) * ConfigVue.TAILLE_CASES;
		}

		return posY;
	}
	
	/**
	 * dessine la case num sur le canvas du plateau.
	 * @param num
	 *          numéro de case (la numérotation commence à 1).
	 */
	private void dessinerCase(final int num) {
		int posX = calculerPosXCase(num);
		int posY = calculerPosYCase(num);

		gcPlateau.strokeRect(posX, posY, ConfigVue.TAILLE_CASES, ConfigVue.TAILLE_CASES);
		String numStr = String.format("%03d", num);
		gcPlateau.setFont(Font.font("Helvetica", FontWeight.BOLD, ConfigVue.TAILLE_POLICE_NUMERO));
		gcPlateau.setFill(Color.BLACK);
		gcPlateau.setStroke(Color.BLACK);
		gcPlateau.fillText(numStr, posX + ConfigVue.DECALAGE_LARGEUR_NUMERO, posY + ConfigVue.DECALAGE_HAUTEUR_NUMERO);
		gcPlateau.strokeText(numStr, posX + ConfigVue.DECALAGE_LARGEUR_NUMERO, posY + ConfigVue.DECALAGE_HAUTEUR_NUMERO);
	}

	/**
	 * affiche la liste des joueurs et la valeur de leurs pions en haut du canvas du plateau.
	 * @param etatPlateau
	 *        contient le plateau qui fournit la liste des joueurs.
	 *          
	 */
	public void listerJoueur(final EtatPlateau etatPlateau) {
		String liste = "Liste des joueurs: ";
		char lettre = 'A';
		boolean premier = true;
		for(Coureur coureur: etatPlateau.getPlateau().getCoureurs() ) {
			if (!premier) {
				liste += ", ";
			}
			liste += coureur.getPseudo() + "(" + lettre + ")";
			premier = false;
			lettre++;
		}

		gcPlateau.setFont(Font.font("Helvetica", FontWeight.BOLD, ConfigVue.TAILLE_POLICE_JOUEUR));
		gcPlateau.setFill(Color.BLACK);
		gcPlateau.setStroke(Color.BLACK);
		gcPlateau.fillText(liste, ConfigVue.DECALAGE_LARGEUR_JOUEUR, ConfigVue.DECALAGE_HAUTEUR_JOUEUR);
		//  gc.strokeText(liste, ConfigVue.DECALAGE_LARGEUR_JOUEUR, ConfigVue.DECALAGE_HAUTEUR_JOUEUR);
	}


	/**
	 * Dessine le pion sur le plateau.
	 * @param lettre
	 *        la lettre représentant le pion.
	 * @param num
	 *        le numéro de la positions (la position de case 1 vaut 0, donc lui ajouter 1 pour calculer la case).
	 */
	private void dessinerPion(final char lettre, final int num) {
		int posX = calculerPosXCase(num + 1);
		int posY = calculerPosYCase(num + 1);
		gcPlateau.setFont(Font.font("Helvetica", FontWeight.BOLD, ConfigVue.TAILLE_POLICE_JOUEUR));
		gcPlateau.setFill(Color.BLACK);
		gcPlateau.setStroke(Color.BLACK);
		gcPlateau.fillText(""+ lettre, posX + 10 , posY + 20 );

	}

	
	/**
	 * Dessine les pions (une lettre) pour chaque coureur.
	 * @param etatPlateau
	 *      contient le plateau qui contient toutes informations.
	 */
	private void placerPions(final EtatPlateau etatPlateau) {
		Map<Coureur, Integer> positions = etatPlateau.getPlateau().getPositions();
		char lettre = 'A';
		String depart = "Départ :";
		String arrivee = "Arrivee :";

		for(Coureur coureur: etatPlateau.getPlateau().getCoureurs()){
			int position = positions.get(coureur);
			switch (position) {
			case Plateau.DEPART: {
				depart += " " + lettre;
				break;
			}
			case Plateau.ARRIVEE: {
				arrivee += " " + lettre;
			}
			default:
				dessinerPion(lettre, position);
			}
			lettre++;
		}
		gcPlateau.setFont(Font.font("Helvetica", FontWeight.BOLD, ConfigVue.TAILLE_POLICE_JOUEUR));
		gcPlateau.setFill(Color.BLACK);
		gcPlateau.setStroke(Color.BLACK);
		gcPlateau.fillText(depart, ConfigVue.DECALAGE_LARGEUR_JOUEUR, 2 * ConfigVue.DECALAGE_HAUTEUR_JOUEUR);

		int nbCases = etatPlateau.getPlateau().getNbCasesPlateau();
		int nbBranches = (nbCases / ConfigVue.NB_CASES_BRANCHE) + 1;

		gcPlateau.fillText(arrivee, ConfigVue.DECALAGE_LARGEUR_JOUEUR, ((nbBranches * 2) + 1) * ConfigVue.TAILLE_CASES + ConfigVue.MARGE_HAUTEUR);


	}


	/**
	 * Affiche la liste des coureurs et les informations à leurs sujets
	 * dans la fenêtre de la console.
	 * @param etatPlateau
	 *        contient le plateau avec toutes les informations.
	 */
	private void afficherListeCoureurs(final EtatPlateau etatPlateau) {
		//	texteConsole.clear();
		Plateau plateau = etatPlateau.getPlateau();
		int numActif = plateau.getNumCoureurActif();
		List<Coureur> coureurs = plateau.getCoureurs();

		for (int num = 0; num < coureurs.size(); num++) {
			String ligne = "";
			Coureur coureur = coureurs.get(num);
			int pos = plateau.getPositions().get(coureur);
			if (num == numActif) {
				ligne += "->";
			}
			ligne += coureur.getPseudo();
			if (coureur.estEpuise()) {
				ligne += "(épuisé " + coureur.getEpuisement() + " tour" + (coureur.getEpuisement() > 1 ? "s)" : ")") ; 
			}
			if (!coureur.isEnCourse()) {
				if (pos == Plateau.PARTI) {
					ligne += " déconnecté ";
				}
				if (coureur.getClassement() > 0) {
					ligne += " Arrivé en position " + coureur.getClassement();
				} 
			} else {
				
				if (pos == Plateau.DEPART) {
					ligne += " au départ";
				}
				else {
					// la position commence à 0, et les cases à 1. Ajoute 1 à pos.
					ligne += " Case " + (pos + 1);
				}
			}
			texteConsole.appendText(ligne + "\n");			
		}

	}
	
	/**
	 * Affiche la partie plateau (liste des joueurs, et le plateau
	 * avec les pions).
	 * @param etatPlateau
	 *       contient le plateau avec toutes les informations.
	 */
	private void afficherPlateau(final EtatPlateau etatPlateau) {
		gcPlateau.clearRect(0, 0, canvasPlateau.getWidth(), canvasPlateau.getHeight());
		int nbCases = etatPlateau.getPlateau().getNbCasesPlateau();
		gcPlateau.setFont(Font.font("Helvetica", FontWeight.LIGHT, 8));
		gcPlateau.setFill(Color.BLACK);
		gcPlateau.setStroke(Color.BLACK);

		for(int posY = 20; posY < 1000; posY += 20) {
			gcPlateau.fillText(posY + "", 20, posY); 
		}

		listerJoueur(etatPlateau);

		for(int num =1; num <= nbCases; num++) {
			dessinerCase(num);
		}

		placerPions(etatPlateau);

	}


	/**
	 * callBack associé au bouton d'interaction avec le joueur actif affiché dans 
	 * la partie bottom.
	 * L'action du callback dépend de l'état du jeu, et donc dépend du dernier
	 * message reçu.
	 * @param event
	 *        event déclenché par le clic de souris sur le bouton.
	 */
	void onBoutonJoueur(MouseEvent event) {
		try {
			switch (lastMessageType) {
			case NOUVEAU_TOUR: {
				// le joueur doit lancer les dés.
				dataCtrl.getConnexion().envoyerLancerDes();
				break;
			}
			case NOTIFICATION_VALEUR_DES: {
				// le joueur doit demander l'avancement de son pion.
				dataCtrl.getConnexion().envoyerDeplacementPion();
				break;
				
			}
			case NOTIFICATION_FIN: {
				// Fermeture de l'application.
				// Ne pas utiliser System.exit() pour terminer une application JavaFX.
				// La terminaison des activités de JavaFX peut ne pas être faite
				// correctement surtout lorqu'il peut y avoir des Service actifs. 
				Platform.exit();
			}
			
			}
		} catch (IOException e) {
			
		}


	}

}
