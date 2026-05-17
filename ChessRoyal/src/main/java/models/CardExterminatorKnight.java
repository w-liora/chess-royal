package models;
///Cavalier qui mange toutes les pièces adverses sur son chemin 
public class CardExterminatorKnight extends Card {
	
	public CardExterminatorKnight() {
		super("ExtarminatorKnight");
	}

	
	@Override
	public void apply(Square[][] board, int row, int col) {
	    Piece piece = board[row][col].getPiece();

	    // Vérifier que la case contient bien un cavalier
	    if (!(piece instanceof Knight)) {
	        return; // on fait rien 
	    }
	    Position pos = new Position(row, col);

	    // Remplacer le Knight par un ExtarminatorKnight au même endroit
	    ExterminatorKnight extarminator = new ExterminatorKnight(pos, piece.getColor());
	    board[row][col].setPiece(extarminator);
	}
}
