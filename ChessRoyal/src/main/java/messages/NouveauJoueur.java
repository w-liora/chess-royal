package main.java.messages;

import java.io.Serializable;
import java.nio.channels.SelectionKey;

/**
 * Classe pour le transport du pseudonyme d'un nouveau joueur.
 * Utilisée par le message {@link ProtocoleMessageType#INFO_NOUVEAU_JOUEUR}
 * 
 * La classe est sérialisable pour permettre son envoi via un réseau.
 */
public class NouveauJoueur implements Serializable{

	/**
	 * Pseudo choisi par le joueur
	 */
	private String pseudo;

	/**
	 * Constructeur de la classe.
	 * @param pseudo Pseudo choisi par le joueur
	 */
	public NouveauJoueur(String pseudo) {
		super();
		this.pseudo = pseudo;
	}

	/**
	 * Getter du pseudo
	 * @return Le pseudo
	 */
	public String getPseudo() {
		return pseudo;
	}
}

