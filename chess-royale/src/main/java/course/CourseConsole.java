package course;

import java.util.List;
import java.util.Scanner;

/**
 * Classe qui permet de tester le modèle sans utilisation du réseau, 
 * ni de l'interface graphique.
 */
public class CourseConsole extends Course{
	public static final String JOUER = "jouer";
	
	/**
	 * Scanner pour les saisies au clavier.
	 */
	private Scanner sc;
	
	
	/**
	 * Contructeur de la version console du modèle.
	 * @param nbCases
	 *         nombre de cases du plateau du jeu.
	 */
	public CourseConsole(int nbCases) {
		super(nbCases);
		sc =  new Scanner(System.in);
	}

	
	private String getNextLine() throws DisconnectedPlayerException {
		try {
		return this.sc.nextLine();
		} catch (IllegalStateException e) {
			throw new DisconnectedPlayerException(this.getPlateau().getNumCoureurActif());
		}
	}

	@Override
	protected Coureur attendreNouveauCoureur() throws Exception {
		System.out.println("Veuillez donner votre pseudo (\"" + JOUER + "\" pour commencer la partie) :");
		String pseudo = getNextLine();
		if (pseudo.equals(JOUER)) {
			return null;
		}
		Coureur coureur = new Coureur(pseudo);
		return coureur;
	}

	@Override
	protected void attendreLancementDes() throws Exception {
		System.out.println("Tapez sur Entrée pour lancer les dès.");
		getNextLine();
	}

	@Override
	protected void attendreDeplacementPion() throws Exception {
		System.out.println("Tapez sur Entrée pour avancer votre pion.");
		getNextLine();
		
	}

	@Override
	protected void notifierNouveauTour() throws Exception {
		Coureur coureur = this.getPlateau().getCoureurActif();
		System.out.println("Nouveau tour de jeu. Tour de " + coureur.getPseudo());
	}

	@Override
	protected void notifierListeCoureur() throws Exception {
		System.out.println("Liste des coureurs:");
		for (Coureur coureur: this.getPlateau().getCoureurs()) {
			System.out.println(coureur.getPseudo());
		}
	}

	@Override
	protected void notifierNumeroCoureur() throws Exception {
		List<Coureur> coureurs = this.getPlateau().getCoureurs();
		
	
		System.out.println("Ordre des tours de jeux:");
		for (int num = 0; num < coureurs.size(); num++) {
			System.out.println("En " + (num + 1) + ":" + coureurs.get(num).getPseudo());
		}
		
	}

	@Override
	protected void notifierDes() throws Exception {
		Coureur coureur = this.getPlateau().getCoureurActif();
		boolean epuise = coureur.estEpuise();
		System.out.println("Valeur du dé 1: " + this.getPlateau().getDe1());
		System.out.println("Valeur du dé 2: " + this.getPlateau().getDe2() + (epuise ? " (dé pas compté, car coureur épuisé)" : ""));
	}

	@Override
	protected void notifierDeplacementPion() throws Exception {
		List<Coureur> coureurs = this.getPlateau().getCoureurs();
		System.out.println("Position des coureurs après déplacement");
		for (Coureur coureur: coureurs) {
			int position = this.getPlateau().getPositions().get(coureur);
			switch (position) {
			case Plateau.ARRIVEE:{
				int classement = coureur.getClassement();
				System.out.println(coureur.getPseudo() + " est arrivé en position " + classement);
				break;
			}
			case Plateau.DEPART: {
				System.out.println(coureur.getPseudo() + " est au départ.");
				break;
			}
			default: {
				System.out.println(coureur.getPseudo() + " est sur la case " + (position + 1));
			}
			
			}
		}

		
	}

	@Override
	protected void notifierFin() throws Exception {
		List<Coureur> coureurs = this.getPlateau().getCoureurs();
		System.out.println("La partie est finie. Classement final:");
		for (Coureur coureur: coureurs) {
			int classement = coureur.getClassement();
			System.out.println(coureur.getPseudo() + " est arrivé en position " + classement);
		}
	
	}

}
