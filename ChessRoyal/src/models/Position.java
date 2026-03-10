package models;

/* 
 * Représente une position sur le plateau
 * La case (0,0) se trouve en haut à gauche du plateau
 * (1,0) représente la première case de la deuxième ligne
 * (0,1) représente le deuxième case de la première ligne
 */

public class Position {
	// (row, column) représente la case dans la matrice du plateau
	private int row; //ligne
	private int column;	//colonne
	
	public Position(int x, int y) {
		row = x;
		column = y;
	}
	
	//renvoie la ligne
	public int getRow() {
		return row;
	}
	
	//renvoie la colonne
	public int getCol() {
		return column;
	}
	
	//Test d'égalité de deux positions (égalité de la ligne et de la colonne)
	public boolean equals(Object o) {
		if (o instanceof Position) {
			Position pos = (Position) o;
			if (pos.row == row && pos.column == column) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
}
