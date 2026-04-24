package models;
import java.util.ArrayList;
import java.lang.Math;

/*
 * Cette classe implémente un pion sur le plateau d'échec
 */

public class Pawn extends Piece {
	private boolean canMoveTwo; //Indique si le pion peut se déplacer de deux cases
								//(ce qui est vrai pour le premier mouvement)
	
	//Constructeur
	public Pawn(Position pos, int col) {
		super(pos,col);
		symbol = 'p'; 
		canMoveTwo = true;
	}
	
	//Redéfinition de la méthode moveTo pour retirer la capacité de se déplacer de deux cases, et gérer le en passant
	public void moveTo(Square[][] board, int x, int y) {
		canMoveTwo = false;
		//On regarde si le pion a réalisé un en passant
		if (Math.abs(this.getCol() - y) == 1 && Math.abs(this.getRow() - x) == 1 && board[x][y].getPiece() == null && board[getRow()][y]!=null && this.canEnPassant(board[getRow()][y].getPiece())) {
			board[getRow()][y].setPiece(null);
		}
		super.moveTo(board, x, y);
	}

	//Indique si ce pion peut effectuer une prise en passant sur la pièce p
	private boolean canEnPassant(Piece p) {
		//On regarde si la pièce est un pion de couleur différente qui vient de se déplacer d'au moins 2 cases verticalement
		return p instanceof Pawn && p.justMoved && p.getColor() != this.getColor() && Math.abs(p.getPrevRow() - p.getRow()) > 1 && p.getPrevCol() == p.getCol();
	}
	
	//Donne la liste des cases vers lesquelles le pion peut se déplacer.
	public Position[] getPossibleMoves(Square[][] board) {
		
		ArrayList<Position> res = new ArrayList<>(); //Liste des cases accessibles
		int x = getRow();
		int y = getCol();
		int col = getColor();
		
		//Si le pion est blanc, il se déplace vers le haut, sinon il se déplace vers le bas,
		//ce qui est indiqué par la variable dir
		int dir = 1;
		if (col==0) {
			dir = -1;
		}
		
		if (x+dir >= 0 && x+dir < board.length) {
		//Toute la suite s'effectue si le pion possède une case devant lui
			
			Square frontSquare = board[x+dir][y]; //On récupère cette case
			if (frontSquare != null && frontSquare.getPiece()==null) { //Le pion ne peut s'y déplacer qui si elle est vide
				res.add(new Position(x+dir, y));
			}
			
			
			if (y>0) { //On regarde s'il existe une case à gauche du pion (et donc devant à gauche)
				Square leftFrontSquare = board[x+dir][y-1]; //On récupère cette case
				if (leftFrontSquare != null) {
					Piece leftFrontPiece = leftFrontSquare.getPiece(); //On récupère la pièce potentielle sur cette case
					if (leftFrontPiece != null && leftFrontPiece.getColor()!=col) { //Le pion ne peut s'y déplacer qui s'il capture
						res.add(new Position(x+dir, y-1));
					}
				}
				
				//On gère maintenant le "en passant" à gauche
				Square leftSquare = board[x][y-1]; 
				if (leftSquare != null) {
					Piece leftPiece = leftSquare.getPiece(); //On récupère la pièce à gauche du pion
					//On l'ajoute si la prise est possible
					if (this.canEnPassant(leftPiece)) {
						res.add(new Position(x+dir,y-1));
					}
				}
				
			}
			
			
			if (y<board.length-1) { //On regarde s'il existe une case à droite du pion
				Square rightFrontSquare = board[x+dir][y+1]; //On récupère cette case
				if (rightFrontSquare != null) {
					Piece rightFrontPiece = rightFrontSquare.getPiece(); //On récupère la pièce potentielle sur cette case
					if (rightFrontPiece != null && rightFrontPiece.getColor()!=col) { //Le pion ne peut s'y déplacer qui s'il capture
						res.add(new Position(x+dir, y+1));
					}
				}
				
				//On gère maintenant le "en passant" à droite
				Square rightSquare = board[x][y+1]; 
				if (rightSquare != null) {
					Piece rightPiece = rightSquare.getPiece(); //On récupère la pièce à droite du pion
					//On l'ajoute si la prise est possible
					if (this.canEnPassant(rightPiece)) {
						res.add(new Position(x+dir,y+1));
					}
				}
			}
			
			
			if (canMoveTwo && x+2*dir >= 0 && x+2*dir < board.length) {
			    Square secondFrontSquare = board[x+2*dir][y];

			    if (frontSquare != null && frontSquare.getPiece() == null &&
			        secondFrontSquare != null && secondFrontSquare.getPiece() == null) {
			        res.add(new Position(x+2*dir, y));
			    }
			}
		}
	
		return res.toArray(new Position[0]); //On renvoie la liste
		
	}
	
	
}

