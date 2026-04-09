package course;

import static common.Log.GEN;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Plateau implements Serializable {
	/**
	 * Valeur de position pour les coureurs au départ.
	 */
	public static final int DEPART = -1;
	/**
	 * Valeur de position pour les coureurs arrivée.
	 */
	public static final int ARRIVEE = -2;
	/**
	 * Valeur de position pour les coureurs ayant quité le jeu avant la fin.
	 */
	public static final int PARTI = -3;
	/**
	 * Valeur maximale d'un dé.
	 */
	public static final int DEMAX = 6;
	
	/**
	 * nombre de cases du plateau de jeu.
	 */
	private int nbCasesPlateau;
	/**
	 * position sur le plateau (obtention de la position par le coureur)
	 */
	private Map<Coureur, Integer> positions;
	/**
	 * position sur le plateau (obtention du coureur par la position)
	 */
	private Coureur [] plateau;
	/**
	 * liste des joueurs dans l'ordre de jeu.
	 */
	private List<Coureur> ordreCoureurs;
	
	/**
	 * numéro du coureur dont c'est le tour de jeu.
	 */
	private int numCoureurActif;
	
	/**
	 * valeur du dé 1.
	 */
	private int de1;
	
	/**
	 * valeur du dé 2. En cas d'épuissement, ce dé n'est pas utilisé dans l'avancement du pion.
	 */
	private int de2;
	

	/**
	 * nombre de coureurs encore en course.
	 */
	private int nbCoureursRestants;
	
	private Random random;


	public Plateau(int nbCasesPlateau) {
		this.nbCasesPlateau = nbCasesPlateau;
		this.plateau = new Coureur[nbCasesPlateau];
		this.positions = new HashMap<>();
		this.ordreCoureurs = new LinkedList<>();
		this.numCoureurActif = 0;
		this.nbCoureursRestants = 0;
		this.random = new Random();
	}
	
	/**
	 * Ajoute un nouveau coureur dans la course. Son pion est placé
	 * au départ.
	 * @param coureur
	 *        le coureur ajouté dans la course.
	 */
	public void ajouterCoureur(Coureur coureur) {
		this.ordreCoureurs.add(coureur);
		this.positions.put(coureur, DEPART);
		this.nbCoureursRestants++;
	}
	
	/**
	 * Retire un coureur de la course. Si le coureur n'est déjà pas
	 * dans le course, cette méthode ne fait rien.
	 * @param coureur
	 *        le coureur retiré de la course.
	 */
	public void retirerCoureurs(final Coureur coureur) {
		if (!this.positions.containsKey(coureur)) {
			// ce joueur n'est pas encore dans la liste.
			// rien à faire.
			return;
		}
		int position = this.positions.get(coureur);
		if (position >= 0) {
			// le pion est sur le plateau. On le retire de sa case.
			plateau[position] = null;
		}
		if ((position != ARRIVEE) && (position != PARTI)) {
			// le joueur participait encore à la course.
			this.nbCoureursRestants--;
		}
		this.positions.put(coureur, PARTI);
		coureur.setEnCourse(false);
	}
	
	/**
	 * retourne le numéro d'ordre d'un coureur.
	 * @param coureur
	 *        le coureur recherché.
	 * @return
	 *        sa position dans la liste d'ordre pour un tour de jeu.
	 *        -1 si le coureur n'est pas dans la liste.
	 */
	public int getNumCoureur(final Coureur coureur) {
		for(int num = 0; num < ordreCoureurs.size(); num++) {
			if (ordreCoureurs.get(num).equals(coureur)) {
				return num;
			}
		}
		return -1;
	}
	
	/**
	 * Donne la liste de tous les coureurs dans l'ordre des tours de jeu.
	 * @return
	 *      la liste des coureurs dans l'ordre des tours de jeu.
	 */
	public List<Coureur> getCoureurs() {
		return this.ordreCoureurs;
	}
	
	/**
	 * Mélange l'odre des coureurs.
	 * @throws InvalidActionException
	 *         exception levée si la liste des coureurs est vide.
	 */
	public void melangerCoureurs() throws InvalidActionException {
		if (ordreCoureurs.size() < 1) {
			throw new InvalidActionException("Impossible de commencer une partie sans coureur connecté");
		}
		List<Coureur> listeMelangee = new LinkedList<>();
		while (ordreCoureurs.size() > 0) {
			int num = random.nextInt() % ordreCoureurs.size();
			Coureur choisi = ordreCoureurs.get(num);
			ordreCoureurs.remove(num);
			listeMelangee.add(choisi);
		}

		ordreCoureurs = listeMelangee;
	}
	
	
	
	
	/**
	 * Fait avancer un coureur de nbCasess sur plateau.
	 * 
	 * @param coureur
	 *      la référence sur le coureur à avancer.
	 * @param nbCases
	 *       le nombre de cases à avancer.
	 * @return
	 *       Le nouvel état de la Map après l'avancement du coureur.
	 */
	public Map<Coureur, Integer> avancerCoureur(Coureur coureur, int nbCases) {
		Integer numCase = positions.get(coureur);
		if (numCase == null) {
			throw new IllegalArgumentException();
		}
		if (numCase != DEPART) {
			// le coureur libère sa place
			plateau[numCase] = null;
		}
		int nouvelleCase = numCase + nbCases;
		if (nouvelleCase >= this.nbCasesPlateau) {
			// le coureur est arrivé
			nouvelleCase = ARRIVEE;
			coureur.setClassement(compterArrives() + 1);
			coureur.setEnCourse(false);
		} else {
			// on avance le coureur en procédant à éventuel
			// échange si la place est occupée
			Coureur enPlace = plateau[nouvelleCase];
			if (enPlace != null) {
				// la place est occupée. On recule l'occupant
				if (numCase != DEPART) {
					plateau[numCase] = enPlace;
				}
				this.positions.put(enPlace, numCase);
			}
			// on avance le coureur
			plateau[nouvelleCase] = coureur;
		}
		this.positions.put(coureur, nouvelleCase);

		return positions;
	}
	
	/**
	 * Compte le nombre de coureurs à l'arrivée.
	 * @return
	 *       le nombre de coureurs à l'arrivée.
	 */
	private int compterArrives() {
		int nbArrives = 0;
		for (int caseNum: positions.values()) {
			if (caseNum == ARRIVEE) {
				nbArrives++;
			}
		}
		return nbArrives;
	}
	
	
	/**
	 * Change la vleur des dés.
	 */
	public void lancerDes()  {
		de1 = random.nextInt(DEMAX) + 1;
		de2 = random.nextInt(DEMAX) + 1;
	}
	
	
	
	/**
	 * getter sur le premier dé.
	 * @return
	 *      la valeur du dé 1.
	 */
	public int getDe1() {
		return de1;
	}

	/**
	 * getter sur le second dé.
	 * @return
	 *      la valeur du dé 2.
	 */
	public int getDe2() {
		return de2;
	}

	/**
	 * Compte le nombre de pions encore en course (ni déjà arrivé, ni parti).
	 * @return
	 *      le nombre de pions encore en course.
	 */
	private int compterEnCourse() {
		int nbEnCourse = 0;
		for(Coureur coureur: ordreCoureurs) {
			if (coureur.isEnCourse()) {
				nbEnCourse++;
			}
		}
		return nbEnCourse;
	}
	
	/**
	 * Indique s'il n'y  plus de pion en jeu.
	 * @return
	 *     true s'il n'y a plus aucun pion en jeu,
	 *     false sinon (tous les pions sont arrivés ou partis).
	 */
	public boolean estFini() {
		return compterEnCourse() == 0;
	}

	/**
	 * getter sur le nombre de cases du plateau.
	 * @return
	 *      le nombre de cases du plateau.
	 */
	public int getNbCasesPlateau() {
		return nbCasesPlateau;
	}


	/**
	 * getteur sur les positions des coureurs sur le plateau.
	 * @return
	 *      les positions de tous les courreurs.
	 */
	public Map<Coureur, Integer> getPositions() {
		return positions;
	}



	public Coureur[] getPlateau() {
		return plateau;
	}

	public void setPlateau(Coureur[] plateau) {
		this.plateau = plateau;
	}
	
	public Coureur getCoureurActif() {
		return this.ordreCoureurs.get(this.numCoureurActif);
	}

	public void changerCoureurActif() throws Exception {
		GEN.trace("Plateau::changerCoureurActif: avant changement, coureur actif : " + numCoureurActif + "( reste " + nbCoureursRestants + " coureurs sur + " + ordreCoureurs.size() + ")" );
		if (nbCoureursRestants == 0) {
			throw new Exception("La partie est terminée");
		}
		do {
			numCoureurActif = (numCoureurActif + 1) % ordreCoureurs.size();
		//	GEN.trace("Plateau::changerCoureurActif: tente " + numCoureurActif );
			
		} while (!ordreCoureurs.get(numCoureurActif).isEnCourse());
		
		GEN.trace("Plateau::changerCoureurActif: après changement, coureur actif : " + numCoureurActif );
	}
	
	public int getNumCoureurActif() {
		return numCoureurActif;
	}

	public void setNumCoureurActif(int numCoureurActif) {
		this.numCoureurActif = numCoureurActif;
	}
	

	@Override
	public String toString() {
		String ordreCoureurStr = "[ ";
		for(Coureur coureur: ordreCoureurs) {
			ordreCoureurStr += coureur.getPseudo() +  "(" + positions.get(coureur) + "), ";
		}
		ordreCoureurStr += "]";
		
		return "Plateau [nbCasesPlateau=" + nbCasesPlateau + ", ordreCoureurs=" + ordreCoureurStr + ", numCoureurActif=" + numCoureurActif
				+ ", de1=" + de1 + ", de2=" + de2 + ", nbCoureursRestants=" + nbCoureursRestants + "]";
	}
	
	
}
