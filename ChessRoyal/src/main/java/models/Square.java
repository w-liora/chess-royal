package main.java.models;

/*
 * Représente une case sur le plateau 
 */

public class Square {
	private Piece piece;	//La pièce présente sur cette case ; null si aucune
	
	//Le constructeur si aucune pièce n'est spécifiée
	public Square() {
		piece=null;
	}
	
	//Le constructeur si on souhaite placer une pièce
	public Square(Piece piece) {
		this.piece=piece;
	}
	
	
	public Piece getPiece() {
		return piece;
	}
	
	//Place une nouvelle pièce sur la case
	public void setPiece(Piece piece) {
		this.piece=piece;
	}
	
	//Supprime la pièce présente sur la case
	public void removePiece() {
		piece=null;
	}
	
	//L'effet de la case. Une case normale n'en possède pas.
	//Modifiable au moment de la création d'une instance (classe anonyme)
	public void effect() {
	}
	
}
