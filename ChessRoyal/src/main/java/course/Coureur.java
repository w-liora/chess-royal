package course;

import java.io.Serializable;
import java.util.Objects;

/**
 * Description d'un coureur.
 */
public class Coureur implements Serializable {
	
	/**
	 * Pseudonyme du joueur.
	 */
	private String pseudo;
	
	
	
	/**
	 * Contructeur qui initialise le coureur.
	 * @param pseudo
	 *        le pseudo du coureur.
	 */
	public Coureur(final String pseudo) {
		super();
		this.pseudo = pseudo;
	}


	public String getPseudo() {
		return pseudo;
	}

	public void setPseudo(final String pseudo) {
		this.pseudo = pseudo;
	}
	

	@Override
	public int hashCode() {
		return Objects.hash(pseudo);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Coureur other = (Coureur) obj;
		// gestion minimaliste d'identification des coureurs: juste leur pseudo.
		return Objects.equals(pseudo, other.pseudo);
	}
	
}
