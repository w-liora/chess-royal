package main.java.models;

import java.util.ArrayList;

public class Bishop extends Piece{
	
	// Constructeur
	public Bishop(Position pos, int col) {
		super(pos, col);
		symbol = 'B';
	}
	
	// Méthode de déplacement
	public Position[] getPossibleMoves(Square[][] board) {
		
		ArrayList<Position> possibleMoves = new ArrayList<>();
		
		int currentRow = this.getRow();
		int currentCol = this.getCol();
		
		int[][] directions = {{-1, -1}, {-1, 1}, {1, 1}, {1, -1}};
		
		for (int[] dir: directions) {
			int newRow = currentRow + dir[0];
			int newCol = currentCol + dir[1];
			
			while (newRow >= 0 && newRow < board.length && newCol >= 0 && newCol < board[0].length) {
				Square testedSquare = board[newRow][newCol];
				
				if (testedSquare == null) {
					break;
				}
				
				if (testedSquare.getPiece() == null) {
					possibleMoves.add(new Position(newRow, newCol));
				}
				
				else {
					Piece targetPiece = testedSquare.getPiece();
					if (targetPiece.getColor() != this.getColor()) {
						possibleMoves.add(new Position(newRow, newCol));
					}
					break;
				}
				
				newRow += dir[0];
				newCol += dir[1];
			}
			
		}
		
		return possibleMoves.toArray(new Position[0]);
	}
	
}