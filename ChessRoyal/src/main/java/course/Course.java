package course;



import static common.Log.GEN;

/**
 * Classe qui implément les régles de la course et qui gère les phases
 * de jeu.
 * Toutes les interactions avec les joueurs sont dans des méthodes
 * abtraites. Leurs mises en œuvre est réalisés dans
 *  - la classe CourseReseau pour le cas du jeu en réseau.
 *  - la classe CourseConsole pour avoir une application locale
 *    facilitant les tests du modèle.
 */
public abstract class Course {

	/**
	 * État de l'automate du jeu.
	 */
	private Etat etat;

	/**
	 * Référence sur le plateau du jeu (il contient toutes
	 * les données du jeu).
	 */
	private Plateau plateau;


	/**
	 * Contructeur de la Course. Prépare un plateau de nbCases cases
	 * et place l'automate du jeu dans son état initial {@link Etat#PHASE_DE_CONNEXION}.
	 * @param nbCases
	 *        le nombre de cases pour le plateau de cette course.
	 */
	public Course(final int nbCases) {
		this.etat = Etat.PHASE_DE_CONNEXION;
		this.plateau = new Plateau(nbCases);
	}


	/**
	 * La boucle qui décrit l'automate du jeu.
	 */
	public void jouer()  {
		GEN.trace("Course::joueur: début de la boucle de jeu");

		while (this.etat != Etat.FIN) {
			GEN.trace("Course::joueur: tour de jeu. Etat = " + this.etat + ", num joueur = " + this.plateau.getNumCoureurActif() + ".");
			try {
				switch (this.etat) {
				case PHASE_DE_CONNEXION: {
					// phase de connexion qui ajoute les nouveaux coureurs
					// à la liste. Se termine sur le réception du message
					// ProtocoleMessageType.DEBUT_PARTIE qui se traduit par
					// un retour null de attendreNouveauCoureur().
					Coureur nouveauCoureur;

					nouveauCoureur = attendreNouveauCoureur();

					if (nouveauCoureur != null) {
						ajouterCoureur(nouveauCoureur);
					} else {
						// fin de la phase de connexion.
						debuterCourse();
						this.etat = Etat.NOTIFICATION_NOUVEAU_TOUR;
						GEN.trace("Course::joueur: passage à l'état " + this.etat );
					}
					continue;
				}
				case NOTIFICATION_NOUVEAU_TOUR: {
					// retour au début d'un tour de jeu (soit en début de partie,
					// soit après l'avancement d'un pion). 
					// Envoie la notification à tous les joueurs.
					notifierNouveauTour();
					this.etat = Etat.ATTENTE_LANCEMENT_DES;
					GEN.trace("Course::joueur: passage à l'état " + this.etat );
					// pas de break, si pas d'exception, on enchaîne la phase suivante.
				}
				case ATTENTE_LANCEMENT_DES: {
					// Attend que le joueur actif clique sur le lancement des dés
					// et lance ceux-ci.
					attendreLancementDes();
					lancerDes();
					this.etat = Etat.NOTIFICATION_DES;
					GEN.trace("Course::joueur: passage à l'état " + this.etat );
					// pas de break, si pas d'exception, on enchaîne la phase suivante
				}
				case NOTIFICATION_DES:
					// Envoie la valeur des dés à tous les joueurs.
					notifierDes();
					this.etat = Etat.ATTENDRE_DEPLACEMENT_PION;
					GEN.trace("Course::joueur: passage à l'état " + this.etat );
					// pas de break, si pas d'exception, on enchaîne la phase suivante

				case ATTENDRE_DEPLACEMENT_PION:
					// Attend que le joueur actif clique sur l'avancement du pion,
					// avance celui-ci.
					attendreDeplacementPion();
					avancerCoureur();
					this.etat = Etat.NOTIFICATION_AVANCEMENT_PION;
					GEN.trace("Course::joueur: passage à l'état " + this.etat );
					// pas de break, si pas d'exception, on enchaîne la phase suivante

				case NOTIFICATION_AVANCEMENT_PION:
					// Envoie le nouvel état du plateau suite à cette avancement.
					notifierDeplacementPion();

					if (plateau.estFini()) {
						this.etat = Etat.FIN;
						GEN.trace("Course::joueur: passage à l'état " + this.etat );
					} else {
						this.etat = Etat.NOTIFICATION_NOUVEAU_TOUR;
						GEN.trace("Course::jouer: passage à l'état " + this.etat );
					}
				}

			}
			catch (DisconnectedPlayerException e) {
				// Gestion de la déconnexion d'un joueur en cours de partie. 
				int numCoureur = e.getNumCoureur();
				if (numCoureur >= 0) {
					Coureur coureur = this.plateau.getCoureurs().get(numCoureur);
					this.plateau.retirerCoureurs(coureur);
					if (numCoureur == this.plateau.getNumCoureurActif()) {
						// c'est le joueur actif qui s'est déconnecté. On passe au joueur suivant.
						if (!plateau.estFini()) {
							try {
								this.plateau.changerCoureurActif();
							} catch (Exception e1) {
								GEN.warn("Course::jouer: impossible de trouver le joueur suivant");
							}
							this.etat = Etat.NOTIFICATION_AVANCEMENT_PION;
						}
					}
				}

			}
			catch (Exception e) {
				// on affiche l'erreur, et retente la phase de jeu (pas de changement d'état)
				GEN.warn("Course::jouer: anomalie durant une phase du jeu:" + e.getMessage());

			}
		}
		terminerCourse();
	}
	
	//################### Les méthodes d'interaction avec les joueurs (atraites)

	/** 
	 * Attend l'arrivée d'un nouveau coureur.
	 * @return
	 *        La référence sur le nouveau coureur si un nouveau coureur rejoint la partie,
	 *        ou null un joueur a déclenché le début de la course. 
	 * @throws Exception
	 *        Toutes les exceptions pouvant se produire.
	 */
	protected abstract Coureur attendreNouveauCoureur() throws Exception;
	/**
	 * Attend que le joueur actif demande le lancement des dés.
	 * @throws Exception
	 *        Toutes les exceptions pouvant se produire.
	 */
	protected abstract void attendreLancementDes() throws Exception;
	/**
	 * Attend que le joueur actif demande l'avancement de son pion.
	 * @throws Exception
	 *        Toutes les exceptions pouvant se produire.
	 */
	protected abstract void attendreDeplacementPion() throws Exception;
	/**
	 * Signale à tous les joueurs qu'un nouveau tour commence, et que
	 * le joueur actif doit demander le lancement des dés.
	 * @throws Exception
	 *        Toutes les exceptions pouvant se produire.
	 */
	protected abstract void notifierNouveauTour() throws Exception;
	/**
	 * Envoie à tous les joueurs déjà connectés la liste des joueurs
	 * connectés après l'arrivée d'un nouveau joueurs. Utilisé durant
	 * la phase de connexion.
	 * @throws Exception
	 *        Toutes les exceptions pouvant se produire.
	 */
	protected abstract void notifierListeCoureur() throws Exception;
	/**
	 * Envoie à tous le joueurs leur numéro dans la liste des coureurs.
	 * Cette notification signale la fin de la phase de connexion.
	 * @throws Exception
	 *        Toutes les exceptions pouvant se produire.
	 */
	protected abstract void notifierNumeroCoureur() throws Exception;
	/**
	 * Envoie à tous les joueurs la valeur des dés qui ont été tirés.
	 * @throws Exception
	 *        Toutes les exceptions pouvant se produire.
	 */
	protected abstract void notifierDes() throws Exception;
	/**
	 * Envoie à tous les joueurs le nouvel état du plateau suite
	 * à l'avancement d'un pion.
	 * @throws Exception
	 *        Toutes les exceptions pouvant se produire.
	 */
	protected abstract void notifierDeplacementPion() throws Exception;
	/**
	 * Envoie à tous les joueurs la notification de la fin de la
	 * partie avec le classement final de la partie.
	 * @throws Exception
	 *        Toutes les exceptions pouvant se produire.
	 */
	protected abstract void notifierFin() throws Exception;


	//################### Les méthodes qui gérent les régles du jeu.
	
	
	/**
	 * Met fin à la course.
	 */
	private void terminerCourse() {
		try {
			notifierFin();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/**
	 * Calcule la valeur des dés.
	 */
	private void lancerDes()  {
		this.plateau.lancerDes();
		GEN.trace("Course::lancerDes: de1 = " + plateau.getDe1() + ", de2 = " + plateau.getDe2() +".");
	}

	/**
	 * Avance le coureur actif en fonction de la valeur des dés
	 * et de l'état d'épuisement du coureur.
	 * @throws Exception
	 *         Exception levée si cette méthode est appelée
	 *         alors qu'il n'y a plus de coureur actif (partie finie).
	 */
	private void avancerCoureur() throws Exception {
		int avancement;
		int de1 = this.plateau.getDe1();
		int de2 = this.plateau.getDe2();

		Coureur coureur = plateau.getCoureurActif();
		if (coureur.estEpuise()) {
			coureur.recuperer();
		}

		if (!coureur.estEpuise()) {
			avancement = de1 + de2;
		} else {
			avancement = de1;
		}

		if (de1 == de2) {
			coureur.forcer();
		} else {
			coureur.reposer();

		}

		plateau.avancerCoureur(coureur, avancement);
		if ( ((de1 != de2) || (!coureur.isEnCourse())) &&
				(!plateau.estFini())) {
			this.plateau.changerCoureurActif();
		}

	}

	/**
	 * Ajoute un coureur à la course, et notifie la nouvelle liste de
	 * coureur à tous les joueurs connectés (y compris le nouveau venu)
	 * @param coureur
	 *          Le nouveau coureur entrant dans la partie.
	 * @throws Exception
	 *          Toutes les exceptions réseaux pouvant se produire lors
	 *          de l'envoi des messages aux joueurs. 
	 */
	private void ajouterCoureur(final Coureur coureur) throws Exception {
		this.plateau.ajouterCoureur(coureur);
		notifierListeCoureur();
	}


	/**
	 * Mélange l'ordre des coureurs, et envoie le numéro d'ordre
	 * à chaque joueur.
	 * @throws Exception
	 *          toutes les exeptions possibles durant le mélange
	 *          ou la notification.
	 */
	private void debuterCourse() throws Exception {
		this.plateau.melangerCoureurs();

		notifierNumeroCoureur();
	}


	public Plateau getPlateau() {
		return plateau;
	}

}
