package models;
//Toute pièce sur cette case disparait
public class CardLavaSquare extends Card{
	
	public CardLavaSquare() {
		super("LavaSquare");
	}
	
	
	
	@Override
	public void apply(Square[][] board, int row, int col) {
	    Square square = board[row][col];

	    // Vérifier que la case existe
	    if (square == null || square.getPiece() != null){
	    	click_legal=false;
	        return; // on fait rien 
	    }
	    click_legal=true;
	    Position pos = new Position(row, col);
	    
	    // Transforme un square (occupé ou pas) en lasquare
	    LavaSquare lavasquare  = new LavaSquare();
	    board[row][col] = lavasquare;
	}

}
