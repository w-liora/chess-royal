package models;
import java.util.ArrayList;

public class King extends Piece {
	int a_bouge; //0 si le roi n'a pas bougé depuis le début, 1 sinon 
	public King(Position pos, int col) {
		super(pos, col);	
		a_bouge=0;
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
				if (piece == null || piece.getColor() != this.getColor()) { // Si la case est vide ou occupée par une pièce adverse, on vérifie si elle n'est pas en échec et s'il n'y a pas un roi a coté
					Position pos_potentiel = new Position(x_potentiel, y_potentiel);
					//SIMULATION DE DEPLACEMENT DU ROI
					// sauvegarde de la pièce cible
					Piece temp = board[x_potentiel][y_potentiel].getPiece();

					// simuler le déplacement
					board[x_potentiel][y_potentiel].setPiece(this);
					board[x_King][y_King].removePiece();

					// vérifier l'échec  - REMARQUE : a chaque fois je recalcule les PossibleMoves des pieces adversaires pour chaque potentiel coup  dans isInCheck, mais je suis obligée, car cette liste peut changer si le roi se déplace en mangeant une piece (car alors, case occupée par Adversaire devient case attaquable par Adversaire) 
					boolean check = isInCheck(board); 
					boolean touchingKing = this.isTouchingKing(board);
					

					// revenir à l’état initial
					board[x_King][y_King].setPiece(this);
					board[x_potentiel][y_potentiel].setPiece(temp);
					if (!check && !touchingKing){
						cases_dispo.add(pos_potentiel);
						}
					}
				}
			}
		return cases_dispo.toArray(new Position[0]);
		}
	
	
	
	public boolean canRoque(Square[][] board, Rook tour) {//Renvoie vrai si le roi peut roquer avec la tour donnée en argument, faux sinon
		if (a_bouge == 0 && tour.a_bouge ==0) { //On vérifie que le roi et la tour concernée n'ont jamais bougé 
			int colonneCur = tour.getCol();
			int ligne=tour.getRow();
			int deplacement;
			if (colonneCur < this.getCol()) {//cas où on traite la tour de gauche
				deplacement = 1;}
			else{//cas où on traite la tour de droite
				deplacement = -1;}
			ArrayList<Position> casesAttaquees = casesAttaquees(board);
			while(colonneCur + deplacement != this.getCol()) {//on regarde si les cases entre roi et tour sont occupées
					if (board[ligne][colonneCur+deplacement].getPiece() != null) { //Si la case est occupée, on ne peut pas roquer
					return false; 
					}
					if(casesAttaquees.contains(board[ligne][colonneCur+deplacement].getPiece().getPos() )){//Si une case entre le roi et la tour est attaquée, le roi peut pas roquer 
						return false;
					}
					colonneCur +=deplacement;
					}			
			}
		else {//Si le roi ou la tour ont deja bougé on peut pas roquer
			return false;}
		if (this.isInCheck(board)){ // Le roi peut pas roquer s'il est en échec 
			return false;
		}
		return true;
	}
	
	
	public boolean isInCheck(Square[][] board) { //Renvoie vraie si le roi est en echec sur le board donné, faux sinon [version beta à cause du cas particulier du pion]
		int x_King = this.getRow(); 
		int y_King = this.getCol();
		for (Square[] row : board) {
		    for (Square s : row) {
		    	if(s!=null && s.getPiece() != null && s.getPiece().getColor() != this.getColor() ) {//Si la case étudiée existe, est non vide et qu'une pièce adverse s'y trouve 
		    		Piece piece = s.getPiece();
		    		Position[] positions = piece.getPossibleMoves(board);//ATTENTION si on est face à un pion, getpossiblemoves != cases attaquées 
                    for (Position pos : positions) {
                        if (pos.getRow() == x_King && pos.getCol() == y_King) {
                            return true; // Le roi est en échec
                        }
                    }
		    	}
		    }
		}
		return false;
	}
	
	
	public boolean isTouchingKing(Square[][] board){
		int x_King = this.getRow();
		int y_King = this.getCol();
		
		//Les déplacements horizontaux et verticaux possibles au roi :
		int[] delta_x = {-1, -1, -1, 0, 0, 1, 1, 1};
	    int[] delta_y = {-1, 0, 1, -1, 1, -1, 0, 1}; 
	    
		for(int i =0; i<8;i++) { //ON regarde dans toutes les directions
			int x_potentiel = x_King + delta_x[i];
			int y_potentiel = y_King + delta_y[i];
			Square s = board[x_potentiel][y_potentiel];
				
			if(x_potentiel >=0  && x_potentiel < board.length && y_potentiel>=0 && y_potentiel < board[0].length && s != null ) { //Je vérifie si la position étudiée n'est pas out of bounds, et si la case existe
				if(s.getPiece() != null) {//Si la case est non vide
                    Piece p = s.getPiece();
                    // Vérifier si c'est un roi adverse
                    if (p instanceof King && p.getColor() != this.getColor()) {
                        return true;
                        }
                    }
				}
			}
		return false;
		}
	
	public ArrayList<Position> casesAttaquees(Square[][] board) { //Renvoie la liste des positions attaquees par l'adversaire

	    ArrayList<Position> casesAttaquees = new ArrayList<>();

	    for (Square[] row : board) {
	        for (Square s : row) {
	        	Piece piece = s.getPiece();
	            if (s != null && piece != null && //Si la case existe, et qu'il y a une pièce dessus, elle est menacée
	                piece.getColor() != this.getColor()) {	                
	                Position[] positions = piece.getPossibleMoves(board);
	                for(Position p : positions){
	                    casesAttaquees.add(p);
	                }
	            }
	        }
	    }
	    return casesAttaquees;
	    }
	}