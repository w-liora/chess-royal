package models;

/*
 * Cette classe abstraite modélise une pièce sur le plateau, par sa position, sa couleur, sa position précédente et son mouvement.
 * Il est nécessaire de créer de nouvelles classes pour chaque pièce qu'on souhaite implanter.
 */

public abstract class Piece {
	private Position pos; //Position de la pièce
	private Position prevPos; //Position précédente de la pièce
	private int color;	//0 pour les blancs, 1 pour les noirs
	
	public Piece(Position pos, int col) {
		this.pos = pos;
		this.prevPos = pos;
		color = col;
	}
	
	//Quelques fonctions pour récupérer les attributs
	public int getRow() {
		return pos.getRow();
	}
	
	public int getCol() {
		return pos.getCol();
	}
	
	public int getPrevRow() {
		return prevPos.getRow();
	}
	
	public int getPrevCol() {
		return prevPos.getCol();
	}
	
	public int getColor() {
		return color;
	}
	//Fin des méthodes get
	
	
	//Méthodes abstraites donnant les déplacements possibles selon le plateau
	//Attention : il s'agit bien d'un plateau de cases. Pensez à récupérer la pièce présente sur chaque case,
	//et vérifier si elle est égale à null.
	public abstract Position[] getPossibleMoves(Square[][] board);
	
	
	//se déplace vers une nouvelle case
	public void moveTo(Position newPos) {
		prevPos = pos;
		pos = newPos;
	}
	
	
	//Changement de couleur
	public void changeColor() {
		if (color == 1) {
			color=0;
		}
		else {
			color=1;
		}
	}
	
}
