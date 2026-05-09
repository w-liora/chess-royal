package main.java.application;

import static main.java.common.Log.COMM;
import static main.java.common.Log.CTRL;


import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import main.java.course.Coureur;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import main.java.reseau.Client;
import main.java.messages.ListeJoueursConnectes;
import main.java.messages.Message;
import main.java.messages.NumeroCoureur;


public class ConnexionCtrl implements javafx.fxml.Initializable {


	//############  partie des attributs décrits dans le fxml généré par SceneBuilder
	@FXML
	private TextField textePseudo;

	@FXML
	private TextField texteHost;

	@FXML
	private TextField textePort;

	@FXML
	private Button boutonCommencer;

	@FXML
	private Button buttonConnexion;
	

	@FXML
	private Label labelEtatConnexion;

	@FXML
	private TextArea texteListeJoueurs;


	//############  partie des attributs ajoutés à la main
	
	/**
	 * Le service utilisé pour gérer les demandes de connexion.
	 */
	private Service<Client> connexionService;
	
	/**
	 * Le service utilisé pour la réception des messages TCP.
	 */
	private Service<Message> receptionService;

	
	/**
	 * Référence sur la classe contenant les références devant être transmises
	 * d'un contrôleur à l'autre lors du changement de vue.
	 * (patron de conception du singleton)
	 */
	private DataCtrl dataCtrl;


	/**
	 * Initialisation du contrôleur, appelée automatiquement par JavaFX après le chargement du FXML.
	 * 
	 * démarre le service de connexion.
	 * 
	 * @param location URL de localisation du fichier FXML
	 * @param resources Ressources externes
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		CTRL.trace("ConnexionCtrl::initialize: initialisation du contrôleur de connexion");
		dataCtrl = DataCtrl.getInstance();
		
		texteListeJoueurs.setEditable(false);
		texteHost.setText(Client.HOST_PAR_DEFAUT);
		textePort.setText(""+ Client.PORT_PAR_DEFAUT);
		boutonCommencer.setManaged(false);
		boutonCommencer.setVisible(false);
		initServiceConnexion();
	}


	//#################   Partie pour la gestion des connexions #################
	/**
	 * Service lançant la tache de connexion.
	 */
	private void initServiceConnexion() {
		// service de connexion
		// La méthode call est mise dans la tâche lancée par le service lors de
		// l'appel de méthode connexionService.start() par le clic di bouton de connexion 
		connexionService = new Service<Client>(){

			@Override
			protected Task<Client> createTask() {

				// Repère 1
				// classe anonyme qui donne le code de la méthode call() pour la connexion
				return new Task<Client>(){

					@Override
					protected Client call() throws Exception {
						CTRL.trace("ConnexionCtrl::connexionService::Task::call:");
						String host = texteHost.getText();
						String portStr = textePort.getText();
						int port = Integer.parseInt(portStr);
						COMM.trace("ConnexionCtrl::connexionService::Task::call: connexion à " + host + ":" + port);
						// cet appel réalise une connexion TCP, qui est un appel bloquant. Il faut donc le
						// placer dans un service.
						return new Client(host, port);
					};

				};

			}
		};


		// repère 2
		// Classe anonyme qui donne le code la méthode handle() en cas de succès de la connexion.
		// Cette méthode handle() est appelée lorsque la méthode call() du service de connexion
		// se termine avec succes. La méthode getValue() permet de retrouver le resultat
		// du call() réussi (la référence du Client connecté au serveur)
		EventHandler<WorkerStateEvent> succeedEvent = new EventHandler<WorkerStateEvent> () {
			@Override
			public void handle(WorkerStateEvent event) {

				try {
					// une fois la méthode call() du service terminée avec succès,
					// la méthode getValue() permet d'obtenir son résultat.
					Client connexion = connexionService.getValue();
					
					// on sauve cette donnée pour pouvoir la transmettre au
					// contrôleur de la seconde vue.
					dataCtrl.setConnexion(connexion);
					
					// on change la vue de l'interface graphique pour prendre en
					// compte la connexion.
					// affichage du status de connexion
					labelEtatConnexion.setText("connecté");
					
					// affichage du bouton pour commencer la course
					boutonCommencer.setManaged(true);
					boutonCommencer.setVisible(true);

					// envoi du pseudo au serveur.
					connexion.envoyerInfoNouveauJoueur(textePseudo.getText());

					// Maintenant que le client est connecté, il peut recevoir des messages
					// du serveur. On lance le service de réception des messages 
					// Il recevra la liste des joueurs connectés envoyée par le serveur
					// à chaque nouvelle connexion, ou bien l'annonce du début de la partie.
					initServiceReception();
					receptionService.start();
				} catch (IOException e) {
					// connexion déjà perdue. On retente une nouvelle connexion.
					dataCtrl.setConnexion(null);
					labelEtatConnexion.setText("connexion perdue");

					// on relance le service de connexion.
					connexionService.restart();
				}


			}
		};

		// repère 3
		// Classe anonyme qui donne le code la méthode handle() en cas d'échec de la connexion.
		// La méthode handle() est appelée lorsque la méthode call() du service de connexion
		// se termine en échec. On pourrait utiliser la méthode getExeption() de la
		// classe Service pour connaître l'exception qui a provoqué cette erreur
		// et éventuellement la traiter.
		EventHandler<WorkerStateEvent> failedEvent = new EventHandler<WorkerStateEvent> () {
			@Override
			public void handle(WorkerStateEvent event) {
				// TODO lancer un popup d'erreur.
				COMM.warn("ConnexionCtrl::connexionService::failedEvent::handle: échec de la connexion");
				dataCtrl.setConnexion(null);
				labelEtatConnexion.setText("connexion échouée");
				// on relance le service de connexion pour traiter une éventuelle nouvelle
				// tentative.
				connexionService.restart();
			}
		};

		// repère 4
		// mise en place du callBack déclenché lors du succès du service de connexion.
		connexionService.setOnSucceeded(succeedEvent);

		// mise en place du callBack déclenché lors de l'échec du service de connexion.
		connexionService.setOnFailed(failedEvent);

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


		EventHandler<WorkerStateEvent> succeedEvent = new EventHandler<WorkerStateEvent> () {
			@Override
			public void handle(WorkerStateEvent event) {
				// la lecture d'un message s'est terminée. On le gére.
				Message message = receptionService.getValue();
				gererMessage(message);

			}
		};

		EventHandler<WorkerStateEvent> failedEvent = new EventHandler<WorkerStateEvent> () {
			@Override
			public void handle(WorkerStateEvent event) {
				COMM.trace("ConnexionCtrl::failedEvent::handle. Echec de la réception d'un message");
				dataCtrl.setConnexion(null);
				labelEtatConnexion.setText("connexion perdue");

				// on relance le service de connexion. (????)
				//connexionService.restart();

			}
		};


		receptionService.setOnSucceeded(succeedEvent);
		receptionService.setOnFailed(failedEvent);

	}


	/**
	 * Gestion des messages reçu par le client.
	 * À ce niveau du protocole (phase de connexion) le client
	 * attend deux types messages:
	 *  - LISTE_JOUEURS_CONNECTES: la liste des joueurs connectée
	 *    mise à jour à chaque connexion d'un noveau joueur au serveur.
	 *  - NUMERO_COUREUR: qui indique le numéro d'ordre du joueur
	 *    dans le tour de jeu. Ce message clôture la phase de connexion.
	 * @param message
	 *        le message reçu.
	 */
	private void gererMessage(final Message message) {
		CTRL.trace("ConnexionCtrl::gererMessage(" + message + ")");


		if (message == null) {
			COMM.trace("ConnexionCtrl::gererMessage: connexion perdue.");
			// connexion perdue. On retente une nouvelle connexion.
			dataCtrl.setConnexion(null);
			labelEtatConnexion.setText("connexion perdue");

			// on relance le service de connexion. ????
			// connexionService.restart();
			return;
		}
		switch (message.getProtocoleMessage()) {
		case LISTE_JOUEURS_CONNECTES: {
			COMM.trace("ConnexionCtrl::gererMessage: réception de LISTE_JOUEURS_CONNECTES.");
			afficherJoueursConnectes((ListeJoueursConnectes) message.getData());
			break;
		}
		case NUMERO_COUREUR: {
			COMM.trace("ConnexionCtrl::gererMessage: réception de NUMERO_COUREUR.");
			NumeroCoureur numeroCoureur = (NumeroCoureur) message.getData();
			dataCtrl.setMonNumero(numeroCoureur.getNumeroCoureur());
			
			// fin de la phase de connexion, on change de vue.
			changerVue();
			return; // on ne relance pas le service de réception:
			// on ne veut plus recevoir de message pour ce contrôleur.
			// le controleur de la nouvelle vue prend le relais.
		}
		default:
			COMM.warn("ConnexionCtrl::gererMessage: réception d'un message non géré: " + message.getProtocoleMessage()+ ".");
			break;
		}

		// on relance le service de réception pour être informé de la
		// connexion des autres joueurs ou du début de partie.
		receptionService.restart();
	}

	/**
	 * Affiche la liste des joueurs connectés.
	 * @param listeJoueurs
	 *        la liste des joueurs connectés.
	 */
	private void afficherJoueursConnectes(final ListeJoueursConnectes listeJoueurs) {
		CTRL.trace("ConnexionCtrl::afficherJoueursConnectes: liste = " + listeJoueurs);
		texteListeJoueurs.clear();
		for(Coureur coureur: listeJoueurs.getCoureurs()) {
			String ligne = coureur.getPseudo() + " connecté.\n";
			texteListeJoueurs.appendText(ligne);
		}

	}

	/**
	 * Changement de la vue.
	 * Cette méthode est appelée à la fin de la phase de connexion,
	 * pour passer à la phase de jeu.
	 */
	private void changerVue() {
		// chargement de la seconde vue.
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PlateauView.fxml"));
			Parent root = loader.load();

			// Injecter la connexion réseau et le numéro du joueur dans le contrôleur du plateau.
			ChessBoardCtrl chessBoardCtrl = loader.getController();
			if (chessBoardCtrl != null) {
				chessBoardCtrl.initReseau(dataCtrl.getConnexion(), dataCtrl.getMonNumero());
			}

			// on retrouve la référence sur la scène grâce à n'importe quel widget du contrôleur.
			Scene scene = boutonCommencer.getScene();
			// on change le contenu de la scène: la nouvelle vue et son contrôleur prennent le relais.
			scene.setRoot(root);
			// on retaille la fenêtre pour qu'elle s'adapte à la nouvelle vue.
			scene.getWindow().sizeToScene();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//#################   Partie pour la gestion des deux boutons de la fenêtre de connexion #################

	/**
	 * Gestion du clic sur le bouton de début de partie.
	 * @param event
	 *        evant associé au clic de souris sur le bouton.
	 */
	@FXML
	void onCommencer(MouseEvent event) {

		try {
			dataCtrl.getConnexion().envoyerDebutDePartie();
		} catch (IOException e) {
			// TODO un popup d'erreur ?
		}
	}

	/**
	 * CallBack appelé lorsque l'on clique sur le bouton qui demande la connexion.
	 * Il démare le service de connexion. La plupart du temps la connexion sera
	 * très rapide, et l'utilisateur ne verra pas de temps d'attente, et ce service
	 * se terminera immédiatement.
	 * @param event
	 *        l'event lié au clic de souris.
	 *         
	 */
	@FXML
	void onConnexion(MouseEvent event) {
		// Lancement du service qui va démarer la méthode call() de la tâche de connexion
		// (donc un nouveau thread).
		connexionService.start();
		// la mathode handle() de succeedEvent ou de faileedEvent sera
		// appelée selon la réussite ou l'échec de cette connexion.
	}



}