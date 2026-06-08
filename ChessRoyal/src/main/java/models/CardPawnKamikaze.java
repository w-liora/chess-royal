package models;

public class CardPawnKamikaze extends Card {
	
	public CardPawnKamikaze() {
        super("PawnKamikaze"); //
    }
	
	@Override
	public void apply(Square[][] board, int row, int col) {
	    Piece piece = board[row][col].getPiece();

	    // Vérifier que la case contient bien un pion 
	    if (!(piece instanceof Pawn)) {
	    	click_legal = false;
	        return; // on fait rien 
	    }
	    click_legal = true;
	    Position pos = new Position(row, col);

	    // Remplacer le Pawn par un PawnKamikaze au même endroit
	    PawnKamikaze kamikaze = new PawnKamikaze(pos, piece.getColor());
	    board[row][col].setPiece(kamikaze);
	}
}
