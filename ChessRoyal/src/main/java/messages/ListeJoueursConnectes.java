package messages;

import java.io.Serializable;
import java.util.List;

import course.Coureur;

/**
 * Classe pour le transport de la liste des joueurs connectés.
 * Utilisée par le message {@link ProtocoleMessageType#LISTE_JOUEURS_CONNECTES}
 * 
 * La classe est sérialisable pour permettre son envoi via un réseau.
 */
public class ListeJoueursConnectes implements Serializable {
	
	/**
	 * La liste des joueurs connectés à transmettre au client.
	 */
	private List<Coureur> coureurs;

	public ListeJoueursConnectes(List<Coureur> coureurs) {
		super();
		this.coureurs = coureurs;
	}

	public List<Coureur> getCoureurs() {
		return coureurs;
	}

	public void setCoureurs(List<Coureur> coureurs) {
		this.coureurs = coureurs;
	}

	@Override
	public String toString() {
		String listeStr = "ListeJoueursConnectes [";
		boolean premier = true;
		for(Coureur coureur: coureurs) {
			if (!premier) {
				listeStr += ", ";
			}
			listeStr += coureur.getPseudo();
			premier = false;
		}
		return listeStr + "]";
	}
	
	

}
