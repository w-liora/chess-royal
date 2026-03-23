package models;

import java.util.ArrayList;

public class Rook extends Piece{
	int did_move; //0 si le roi n'a pas bougé depuis le début, 1 sinon 
	public Rook(Position pos, int col) {
		super(pos, col);
		did_move=0;
	}
	
	
	public void moveTo(Position newPos) {
		super.moveTo(newPos);
		did_move=1;
	}
	
	
	
	public Position[] getPossibleMoves(Square[][] board) { //REMARQUE : quand la tour bouge, il faut mettre son attribut a_bouge à 1 (utile pour la fonction de roque)
		
		int x_Rook = this.getRow();
		int y_Rook = this.getCol();
		int[][] delta = {{-1,0}, { 1, 0}, {0,-1}, {0,1}}; //Directions (verticales ou horizontales)
		ArrayList<Position> possibleMoves = new ArrayList<>(); // liste des cases accessibles
		
	for (int[] pos : delta) { //Parcours horizontal avec la tour
		
		int x_potentiel = x_Rook + pos[0];
		int y_potentiel = y_Rook+pos[1]; 
		while(x_potentiel >=0 && x_potentiel < board.length && y_potentiel >= 0 && y_potentiel < board[0].length) { //On vérifie qu'on n'est pas out of bounds
			Square caseCur = board[x_potentiel][y_potentiel];
			if (caseCur != null) { // On vérifie que la case existe 
				Piece pieceCur = caseCur.getPiece();
				if (pieceCur==null) {//On regarde si la case est vide 
					possibleMoves.add( new Position(x_potentiel, y_potentiel) ); //Si la case est vide, on l'ajoute à nos possibilités et on continue
				}
				else {//Si la case est occupée par une pièce adverse, on l'ajoute a nos possibilités et on arrête, sinon on arrête directement
				    if (pieceCur.getColor() != this.getColor()) {
				        possibleMoves.add(new Position(x_potentiel, y_potentiel));
				    }
				    break;
					}
				}
			else {//Si la case n'existe pas on arrête 
				break; 
			}
			x_potentiel = x_potentiel + pos[0];
			y_potentiel = y_potentiel + pos[1];			
			}
		}
	return possibleMoves.toArray(new Position[0]);
	}
	

	
}
