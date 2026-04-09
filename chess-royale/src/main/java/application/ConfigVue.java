package application;

import messages.EtatPlateau;

/**
 * Classe qui configure les éléments de la vue.
 */
public class ConfigVue {
	
	public static final int TAILLE_DE = 50;
	public static final int MARGE_DE = 20;
	public static final int MARGE_HAUTEUR_BOTTOM = MARGE_DE;	
	public static final String IMAGES_DIR = "/images";
	public static final String FORMAT_IMAGES_DE = "de_rouge_%d.png";
	public static final String FORMAT_TEXTE_DE = "dé %d";
	public static final int TAILLE_POLICE_DE = 12;
	public static final int MARGE_TEXTE_DE_LARGEUR = TAILLE_DE / 4;
	public static final int MARGE_TEXTE_DE_HAUTEUR = TAILLE_POLICE_DE;
	
	public static final int DECORATION_HAUTEUR = 50;
	public static final int BOTTOM_HAUTEUR = 100;
	public static final int LEFT_LARGEUR = 100;
	public static final int CONSOLE_LARGEUR = 300;
	public static final int MARGE_HAUTEUR = 120;
	public static final int MARGE_LARGEUR = 40;
	
	public static final String LANCER_DE = "Lancer les dés";
	public static final String AVANCER_PION = "Avancer le pion";
	public static final String FERMER_JEU = "Fermer le jeu";

	
	// coté des cases (carrées: donc largeur et hauteur 
	public static final int TAILLE_CASES = 40; 
	public static final int NB_CASES_LIGNE = 25;
	public static final int NB_CASES_COL = 1;
	
	
	
	
	public static final int TAILLE_POLICE_NUMERO = 15;
	public static final int DECALAGE_HAUTEUR_NUMERO = (TAILLE_CASES * 7) / 8;
	public static final int DECALAGE_LARGEUR_NUMERO = TAILLE_CASES / 8;
	public static final int DECALAGE_HAUTEUR_PION = TAILLE_CASES / 8;
	public static final int DECALAGE_LARGEUR_PION = (TAILLE_CASES * 2) / 8;
	public static final int MARGE_BOTTON = CONSOLE_LARGEUR / 2;
	
	public static final int TAILLE_POLICE_JOUEUR = 15;
	public static final int DECALAGE_HAUTEUR_JOUEUR = 40;
	public static final int DECALAGE_LARGEUR_JOUEUR = MARGE_LARGEUR;
	
	// constantes calculées à partir des constantes de base
	
	public static final int NB_CASES_BRANCHE = NB_CASES_COL + NB_CASES_LIGNE;
	public static final int LARGEUR_BRANCHE = TAILLE_CASES * NB_CASES_LIGNE;
	public static final int HAUTEUR_BRANCHE = TAILLE_CASES * (NB_CASES_COL + 1);
	public static final int PLATEAU_LARGEUR = CONSOLE_LARGEUR + 2 * MARGE_LARGEUR + NB_CASES_LIGNE * TAILLE_CASES;
	
	

}
