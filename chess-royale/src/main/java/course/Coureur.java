package course;

import java.io.Serializable;
import java.util.Objects;

/**
 * Description d'un coureur.
 */
public class Coureur implements Serializable {
	/**
	 * Nombre de doubles à partir du moment où le coureur est épuisé.
	 */
	public static final int EFFORT_MAX = 2;
	/**
	 * Nombre de tours sans double pour récpérer après épuisement.
	 */
	public static final int RECUPERATION = 4;
	
	/**
	 * Pseudonyme du joueur.
	 */
	private String pseudo;
	
	/**
	 * nombre de tours où le coureur est encore épuisé.
	 */
	private int epuisement;
	
	/**
	 * nombre de doubles consécutifs.
	 */
	private int effort;
	
	/**
	 * classement du joueur à l'arrivée.
	 */
	private int classement;
	
	/**
	 * vrai si le coureur est encore en course.
	 */
	private boolean enCourse;

	
	/**
	 * Contructeur qui initialise le coureur.
	 * @param pseudo
	 *        le pseudo du coureur.
	 */
	public Coureur(final String pseudo) {
		super();
		this.pseudo = pseudo;
		this.epuisement = 0;
		this.effort = 0;
		this.classement = 0;
		this.enCourse = true;
	}

	public int getClassement() {
		return classement;
	}

	public void setClassement(final int classement) {
		this.classement = classement;
	}

	public String getPseudo() {
		return pseudo;
	}

	public void setPseudo(final String pseudo) {
		this.pseudo = pseudo;
	}
	
	public void epuiser() {
		this.epuisement = RECUPERATION;
	}
	
	/**
	 * réduit l'épuisement si celui-ci n'est pas nul.
	 * Cette méthode est appélée lors du jet de dés
	 * avec deux valeurs différentes. 
	 */
	public void recuperer() {
		if (this.epuisement > 0) {
			this.epuisement--;
		}
	}

	public int getEpuisement() {
		return epuisement;
	}
	
	public boolean estEpuise() {
		return epuisement > 0;
	}

	/**
	 * Indique que le joueur relance les dés suite à un double.
	 * Si cette méthode est appelée EFFORT_MAX sans appel de
	 * reposer(), le coureur devient épuisé pour RECUPERATION
	 * lancements de dés (seul le dé 1 est utilisé pour 
	 * calculer l'avancement d'un coureur épuisé).
	 */
	public void forcer() {
		this.effort++;
		if (this.effort >= EFFORT_MAX) {
			this.epuiser();
			this.effort = 0;
		}
	}

	/**
	 * Annule les efforts accumulés. 
	 * Cette méthode est appélée lors du jet de dés
	 * avec deux valeurs différentes. 
	 */
	public void reposer() {
		this.effort = 0;
	}
	
	
	public boolean isEnCourse() {
		return enCourse;
	}

	public void setEnCourse(boolean enCourse) {
		this.enCourse = enCourse;
	}

	@Override
	public String toString() {
		String res = pseudo;
		if (epuisement > 0) {
			res += " (-" + epuisement + ")";
		}
		if (classement > 0) {
			res += " arrivé (" + classement + ")";
		}
		return res; //+ ", epuisement=" + epuisement + ", classement=" + classement + "]";
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
