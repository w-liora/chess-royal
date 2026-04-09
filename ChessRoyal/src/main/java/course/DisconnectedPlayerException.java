package course;

/**
 * Exception levée si un joueur quitte la partie en cours de jeu.
 */
public class DisconnectedPlayerException extends Exception {
	/**
	 * numéro du coureur dans l'ordre des tours.
	 */
	private int numCoureur;

	/**
	 * 
	 * @param numCoureur
	 *       numero du coureur dans l'ordre des tours.
	 */
	public DisconnectedPlayerException(int numCoureur) {
		super();
		this.numCoureur = numCoureur;
	}

	
	public int getNumCoureur() {
		return numCoureur;
	}
	
	
	
	
}
