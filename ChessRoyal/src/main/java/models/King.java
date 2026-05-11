package models;
import java.util.ArrayList;

public class King extends Piece {
	int did_move; //0 si le roi n'a pas bougé depuis le début, 1 sinon 
	
	public King(Position pos, int col) {
		super(pos, col);	
		did_move=0;
		symbol = 'K';
	}
	
	
	
	
	@Override
	public Position[] getPossibleMoves(Square[][] board) { //REMARQUE : quand le roi bouge, il faut mettre son attribut a_bouge à 1 (utile pour la fonction de roque)
		ArrayList<Position> cases_dispo = new ArrayList<>(); //J'utilise un arraylist car je ne sais pas encore combien de positions seront dispo, donc je ne connais pas encore les dimensions pour un tableau 
		int x_King = this.getRow();
		int y_King = this.getCol();
		
		//Les déplacements horizontaux et verticaux possibles au roi :
		int[] delta_x = {-1, -1, -1, 0, 0, 1, 1, 1};
	    int[] delta_y = {-1, 0, 1, -1, 1, -1, 0, 1}; 
	    
		for(int i =0; i<8;i++) { //ON regarde dans toutes les directions
			int x_potentiel = x_King + delta_x[i];
			int y_potentiel = y_King + delta_y[i];
			
			if(x_potentiel >=0  && x_potentiel < board.length && y_potentiel>=0 && y_potentiel < board[0].length && board[x_potentiel][y_potentiel] != null ) { //Je vérifie si la position étudiée n'est pas out of bounds, et si la case existe  
				Piece piece = board[x_potentiel][y_potentiel].getPiece();
				if (piece == null || piece.getColor() != this.getColor()) { // Si la case est vide ou occupée par une pièce adverse, on a le droit d'y aller
					Position pos_potentiel = new Position(x_potentiel, y_potentiel);
						cases_dispo.add(pos_potentiel);
					}
				}
			}
		//On gère ici le roque
		if (did_move == 0) {
		    int row = this.getRow();
		    int col = this.getCol();

		    // grand roque
		    if (col - 4 >= 0) {
		        Piece coin1 = board[row][col - 4].getPiece();
		        if (coin1 instanceof Rook) {
		            if (this.canCastle(board, (Rook) coin1)) {
		                cases_dispo.add(new Position(row, col - 2));
		            }
		        }
		    }

		    // petit roque
		    if (col + 3 < board[0].length) {
		        Piece coin2 = board[row][col + 3].getPiece();
		        if (coin2 instanceof Rook) {
		            if (this.canCastle(board, (Rook) coin2)) {
		                cases_dispo.add(new Position(row, col + 2));
		            }
		        }
		    }
		}
		
		return cases_dispo.toArray(new Position[0]);
		}
	
	
	public void moveTo(Square[][] board, int x, int y) {
		did_move=1;
		if (y-this.getCol() ==2) { //On déplace la tour s'il s'agit d'un roque
			board[this.getRow()][this.getCol()+3].getPiece().moveTo(board, x, y-1);
		}
		if (y-this.getCol() == -2) {
			board[this.getRow()][this.getCol()-4].getPiece().moveTo(board, x, y+1);
		}
		super.moveTo(board, x, y);
	}
	
	
	public boolean canCastle(Square[][] board, Rook tour) {//Renvoie vrai si le roi peut roquer avec la tour donnée en argument, faux sinon
		if (did_move == 0 && tour.did_move ==0) { //On vérifie que le roi et la tour concernée n'ont jamais bougé 
			int colonneCur = tour.getCol();
			int ligne=tour.getRow();
			int deplacement;
			if (colonneCur < this.getCol()) {//cas où on traite la tour de gauche
				deplacement = 1;}
			else{//cas où on traite la tour de droite
				deplacement = -1;}
			while(colonneCur + deplacement != this.getCol()) {//on regarde si les cases entre roi et tour sont occupées
					if (board[ligne][colonneCur+deplacement].getPiece() != null) { //Si la case est occupée, on ne peut pas roquer
					return false; 
					}
					colonneCur +=deplacement;
					}			
			}
		else {//Si le roi ou la tour ont deja bougé on peut pas roquer
			return false;
			}
		return true;
	}
	
		
	}