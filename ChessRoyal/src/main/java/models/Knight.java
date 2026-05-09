package main.java.models;

import java.util.ArrayList;

public class Knight extends Piece{
	
	//Constructeur
	public Knight(Position pos, int col) {
		super(pos, col);
		symbol = 'N';
	}

	// Méthode de déplacement
	public Position[] getPossibleMoves(Square[][] board) {
			
		ArrayList<Position> possibleMoves = new ArrayList<>(); // liste des cases accessibles
			
		// On récupère la position du cavalier
		int currentRow = this.getRow();
		int currentCol = this.getCol();
			
		//Huit directions qui correspondes aux différents déplacements du cavalier
		int[][] directions = {{-2, -1}, {-2, 1}, {2, -1}, {2, 1},{-1, -2}, {-1, 2}, {1, -2}, {1, 2}};
		for (int[] dir: directions) {
			int newRow = currentRow + dir[0];
			int newCol = currentCol + dir[1];
			if(newRow >= 0 && newRow < board.length && newCol >= 0 && newCol < board.length && board[newRow][newCol] != null) {
				Square testedSquare = board[newRow][newCol];
				
				if (testedSquare.getPiece() == null) {
					possibleMoves.add(new Position(newRow, newCol)); // On ajoute la case si il n'y a pas de pièce
				}
				
				else {
					Piece targetPiece = testedSquare.getPiece(); // on récupère la pièce sur la case a occuper
					if (targetPiece.getColor() != this.getColor()) {
						possibleMoves.add(new Position(newRow, newCol)); // si c'est une pièce adverse on peut la manger et prendre sa place
					}
				}
			}
		}
		return possibleMoves.toArray(new Position[0]);
	}
}
