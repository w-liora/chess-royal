package models;

import java.util.ArrayList;

public class Reine extends Piece{
	
	// Constructeur
	public Reine(Position pos, int col) {
		super(pos, col);
	}
	
	// Méthode de déplacement
	public Position[] getPossibleMoves(Square[][] board) {
		
		ArrayList<Position> possibleMoves = new ArrayList<>(); // liste des cases accessibles
		
		// On récupère la position de la reine
		int currentRow = this.getRow();
		int currentCol = this.getCol();
		
		//Huit directions qui correspondes aux diagonales (hg, hd, bd, bg) et haut, bas, gauche, droite
		int[][] directions = {{-1, -1}, {-1, 1}, {1, 1}, {1, -1}, {1, 0}, {-1, 0}, {0, -1}, {0, 1}};
		
		for (int[] dir: directions) {
			int newRow = currentRow + dir[0];
			int newCol = currentCol + dir[1];
			
			//boucle qui teste toutes les possibilités tant que l'on reste dans les limites du plateau
			// Avec board.length qui représente le nb de col et row sur notre échéquier de taille extensible
			while (newRow >= 0 && newRow < board.length && newCol >= 0 && newCol < board.length) {
				Square testedSquare = board[newRow][newCol];
				
				if (testedSquare.getPiece() == null) {
					possibleMoves.add(new Position(newRow, newCol)); // On ajoute la case si il n'y a pas de pièce
				}
				
				else {
					Piece targetPiece = testedSquare.getPiece(); // on récupère la pièce sur le chemin
					if (targetPiece.getColor() != this.getColor()) {
						possibleMoves.add(new Position(newRow, newCol)); // si c'est une pièce adverse on peut la manger et prendre sa place
					}
					
					break; // On arrete de parcourir les cases dans cette direction	
				}
				
				newRow += dir[0];
				newCol += dir[1];
			}
			
		}
		
		return possibleMoves.toArray(new Position[0]);
	}
	
}
