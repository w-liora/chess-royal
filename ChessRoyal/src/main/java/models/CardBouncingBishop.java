package main.java.models;

/**
 * Carte qui transforme un fou allié en BouncingBishop.
 */
public class CardBouncingBishop extends Card {

    public CardBouncingBishop() {
        super("BouncingBishop");
    }

    @Override
    public void apply(Square[][] board, int row, int col) {
        Piece piece = board[row][col].getPiece();

        // Il faut cibler un fou existant
        if (!(piece instanceof Bishop)) {
            return;
        }

        // On remplace le Bishop par un BouncingBishop de même couleur/position
        Position pos = new Position(row, col);
        BouncingBishop bouncingBishop = new BouncingBishop(pos, piece.getColor());
        board[row][col].setPiece(bouncingBishop);
    }
}