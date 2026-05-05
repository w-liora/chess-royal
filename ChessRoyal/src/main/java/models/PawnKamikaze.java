
package models;
import java.util.ArrayList;
import java.lang.Math;

/*
 * Cette classe implémente un pion sur le plateau d'échec
 */

public class PawnKamikaze extends Pawn {
	private int compteur;

	
	//Constructeur
	public PawnKamikaze(Position pos, int col) {
		super(pos,col);
		compteur = 0;
		this.symbol = 'T';
	}
	
	

	//Redéfinition de la méthode moveTo pour retirer la capacité de se déplacer de deux cases, et gérer le en passant
	public void moveTo(Square[][] board, int x, int y) {
		canMoveTwo = false;
		compteur = compteur +1;
		//On regarde si le pion a réalisé un en passant
		if (Math.abs(this.getCol() - y) == 1 && Math.abs(this.getRow() - x) == 1 && board[x][y].getPiece() == null && board[getRow()][y]!=null && this.canEnPassant(board[getRow()][y].getPiece())) {
			board[getRow()][y].setPiece(null);
		}
		CanPromotion = (x == PromotionRow); //Si le pion est sur la rangée de promotion, on change la valeur du booléen
		super.moveTo(board, x, y);
		
		if (compteur == 2) {
		    
		    int px = getRow();
		    int py = getCol();

		    // Parcourir les 8 cases autour du pion
		    for (int dx = -1; dx <= 1; dx++) {
		        for (int dy = -1; dy <= 1; dy++) {
		            if (dx == 0 && dy == 0) continue; // ignorer la case du pion lui-même

		            int nx = px + dx;
		            int ny = py + dy;

		            // vérifier que la case existe dans board
		            if (nx >= 0 && nx < board.length && ny >= 0 && ny < board[0].length) {
		                board[nx][ny].setPiece(null); // faire sauter la pièce
		            }
		        }
		    }
		    // sacrifice de la pièce qui disaparait
		    board[px][py].setPiece(null);
		}}


	

	
	
}


