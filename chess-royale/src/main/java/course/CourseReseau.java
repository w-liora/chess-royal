package course;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static common.Log.COMM;
import static common.Log.GEN;

import messages.NumeroCoureur;
import messages.EtatPlateau;
import messages.ListeJoueursConnectes;
import messages.Message;
import messages.MessageSigne;
import messages.ProtocoleMessageType;
import messages.NouveauJoueur;
import reseau.Serveur;

/**
 * Mise en œuvre des méthodes abtraites de la classe Course pour le
 * cas de l'implémentation pour le réseau.
 */
public class CourseReseau extends Course {
	/**
	 * Map pour rétrouver l'id client ce chaque coureur pour vérifier que les
	 * messages réçus proviennnent bien du client attendu.
	 */
	private Map<Coureur, SelectionKey> idReseauParCoureur;

	/**
	 * Map pour rétrouver le coureur associé à l'idRseau d'un message reçu.
	 */
	private Map<SelectionKey, Coureur> coureurParIdReseau;

	/**
	 * Référence sur la classe Serveur gérant toutes les connexions des clients. 
	 */
	private Serveur serveur;

	/**
	 * Contructeur de la classe de Course utilisée pour l'implémentation avec réseau. 
	 * @param nbCases
	 *         le nombre de cases de la course.
	 * @param port
	 *         le port utilisé par le serveur utilisé pour l'implémentation avec réseau.
	 * @throws IOException
	 *         Toutes les exceptions qui peuvent se produire lors de la création
	 *         du socket du serveur.
	 */
	public CourseReseau(final int nbCases, final int port) throws IOException {
		super(nbCases);
		this.serveur = new Serveur(port);
		idReseauParCoureur = new HashMap<Coureur, SelectionKey>();
		coureurParIdReseau = new HashMap<SelectionKey, Coureur >();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Dans le cadre de l'implémentation réseau, cette méthode:
	 *  <ul>
	 *    <li> soit reçoit la connexion d'un nouveau client qui retourne un message
	 *     avec les informations sur le nouveau courreur {@link ProtocoleMessageType#INFO_NOUVEAU_JOUEUR}.
	 *     Dans ce cas, la méthode retourne une référence contenant les informations 
	 *     de ce coureur.
	 *     <li> soit reçoit le message {@link ProtocoleMessageType#DEBUT_PARTIE}. Dans ce cas
	 *     la méthode retourne null. 
	 *    </li> 
	 *  </ul> 
	 * </p>
	 */
	@Override
	protected Coureur attendreNouveauCoureur() throws Exception {

		MessageSigne messageSigne = serveur.lireMessage();
		gererDeconnexionCoureur(messageSigne);

		// premier message d'un nouveau coureur connecté.
		if (messageSigne.getProtocoleMessageType() == ProtocoleMessageType.INFO_NOUVEAU_JOUEUR) {
			NouveauJoueur nouveau = (NouveauJoueur) messageSigne.getData();
			Coureur coureur = new Coureur(nouveau.getPseudo());
			idReseauParCoureur.put(coureur, messageSigne.getIdReseau());
			coureurParIdReseau.put(messageSigne.getIdReseau(), coureur);
			return coureur;

		}

		// message de début de partie mettant fin à la phase de connexion.
		if (messageSigne.getProtocoleMessageType() == ProtocoleMessageType.DEBUT_PARTIE) {			
			return null;
		}

		throw new InvalidActionException("Un message non géré est arrivé durant la mise en place des joueurs");
	}

	@Override
	protected void notifierListeCoureur() throws Exception {
		ListeJoueursConnectes listeJoueurs = new ListeJoueursConnectes(this.getPlateau().getCoureurs());
		COMM.trace("CourseReseau::notifierListeCoureur: " + listeJoueurs);

		Message message = new Message(listeJoueurs, ProtocoleMessageType.LISTE_JOUEURS_CONNECTES);
		serveur.envoyerATous(message);
	}

	@Override
	protected void notifierNumeroCoureur() throws Exception {
		// on interdit les nouvelles connexions.
		serveur.cloreServerSocketChannel();

		List<Coureur> coureurs = this.getPlateau().getCoureurs();

		// on notifie tous les clients pour qu'ils changent de vue et
		// leur donner leur numéro.
		for (int num = 0; num < coureurs.size(); num++) {
			NumeroCoureur numeroCoureur = new NumeroCoureur(num);
			Message message = new Message(numeroCoureur, ProtocoleMessageType.NUMERO_COUREUR);
			SelectionKey idReseau = idReseauParCoureur.get(coureurs.get(num));
			serveur.envoyerMessage(idReseau, message);
		}
	}

	@Override
	protected void notifierNouveauTour() throws Exception {

		EtatPlateau etatPlateau = new EtatPlateau(this.getPlateau());
		Message message = new Message(etatPlateau, ProtocoleMessageType.NOUVEAU_TOUR);
		serveur.envoyerATous(message);
	}

	private void gererDeconnexionCoureur(final MessageSigne messageSigne) throws DisconnectedPlayerException {
		if (messageSigne.getProtocoleMessageType() == ProtocoleMessageType.DECONNEXION) {
			SelectionKey idReseau = messageSigne.getIdReseau();
			Coureur coureur = coureurParIdReseau.get(idReseau);
			int numCoureur = this.getPlateau().getNumCoureur(coureur);
			coureurParIdReseau.remove(idReseau);
			idReseauParCoureur.remove(coureur);
			throw new DisconnectedPlayerException(numCoureur);
		}
	}

	@Override
	protected void attendreLancementDes() throws Exception {
		MessageSigne messageSigne = serveur.lireMessage();
		gererDeconnexionCoureur(messageSigne);

		Coureur coureur = this.getPlateau().getCoureurActif();
		SelectionKey idReseau = idReseauParCoureur.get(coureur);
		if (! idReseau.equals(messageSigne.getIdReseau())) {
			throw new InvalidActionException("Un joueur a fait une action alors que ce n'était pas son tour");
		}

		if (messageSigne.getProtocoleMessageType() != ProtocoleMessageType.DEMANDE_LANCEMENT_DES) {
			throw new InvalidActionException("Un joueur n'a pas fait l'action attendue de lui (lancer les dés)");

		}

		return;
	}

	@Override
	protected void notifierDes() throws Exception {
		EtatPlateau etatPlateau = new EtatPlateau(this.getPlateau());
		Message message = new Message(etatPlateau, ProtocoleMessageType.NOTIFICATION_VALEUR_DES);
		serveur.envoyerATous(message);
	}


	@Override
	protected void notifierFin() throws Exception {
		EtatPlateau etatPlateau = new EtatPlateau(this.getPlateau());
		Message message = new Message(etatPlateau, ProtocoleMessageType.NOTIFICATION_FIN);
		serveur.envoyerATous(message);

	}

	@Override
	protected void attendreDeplacementPion() throws Exception {
		MessageSigne messageSigne = serveur.lireMessage();
		gererDeconnexionCoureur(messageSigne);
		
		Coureur coureur = this.getPlateau().getCoureurActif();
		if (! idReseauParCoureur.get(coureur).equals(messageSigne.getIdReseau())) {
			throw new InvalidActionException("Un joueur a fait une action alors que ce n'était pas son tour");
		}


		if (messageSigne.getProtocoleMessageType() != ProtocoleMessageType.DEMANDE_DEPLACEMENT_PION) {
			throw new InvalidActionException("Un joueur n'a pas fait l'action attendue de lui (avancer le pion)");

		}

		// tout est OK.
		return;

	}

	@Override
	protected void notifierDeplacementPion() throws Exception {
		EtatPlateau etatPlateau = new EtatPlateau(this.getPlateau());
		Message message = new Message(etatPlateau, ProtocoleMessageType.NOTIFICATION_DEPLACEMENT_PION);
		serveur.envoyerATous(message);
	}



}
