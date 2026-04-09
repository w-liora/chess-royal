package messages;

import java.io.Serializable;
import course.Plateau;

/**
 * Classe pour le transport du plateau dans les
 * messages émis lors d'un changement du plateau.
 */
public class EtatPlateau implements Serializable {
	
	/**
	 * le plateau de jeu à transmettre au client.
	 */
	private Plateau plateau;

	public EtatPlateau(final Plateau plateau) {
		super();
		this.plateau = plateau;
	}

	public Plateau getPlateau() {
		return plateau;
	}

	public void setPlateau(Plateau plateau) {
		this.plateau = plateau;
	}
	
	

}
