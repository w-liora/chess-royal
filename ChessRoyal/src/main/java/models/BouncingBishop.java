package models;

import java.util.ArrayList;

/**
 * Fou spécial pouvant "rebondir" sur les murs latéraux (gauche/droite).
 */
public class BouncingBishop extends Piece {

    public BouncingBishop(Position pos, int col) {
        super(pos, col);
        symbol = 'G'; // pour BouncingBishop
    }

    @Override
    public Position[] getPossibleMoves(Square[][] board) {
        ArrayList<Position> moves = new ArrayList<>();

        int currentRow = this.getRow();
        int currentCol = this.getCol();

        // 4 diagonales de base
        int[][] directions = {
            {-1, -1}, // haut-gauche
            {-1,  1}, // haut-droite
            { 1,  1}, // bas-droite
            { 1, -1}  // bas-gauche
        };

        for (int[] dir : directions) {
            int dRow = dir[0];
            int dCol = dir[1];

            int row = currentRow + dRow;
            int col = currentCol + dCol;

            boolean rebondFait = false;

            while (row >= 0 && row < board.length && col >= 0 && col < board[0].length) {
                Square sq = board[row][col];
                if (sq == null) break;

                Piece target = sq.getPiece();

                if (target == null) {
                    moves.add(new Position(row, col));
                } else {
                    // Capture autorisée seulement sur ennemi, puis arrêt du rayon
                    if (target.getColor() != this.getColor()) {
                        moves.add(new Position(row, col));
                    }
                    break;
                }

                // Si on est sur un mur latéral et rebond pas encore fait -> inverser horizontalement
                if (!rebondFait && (col == 0 || col == board[0].length - 1)) {
                    dCol = -dCol;
                    rebondFait = true;
                }

                row += dRow;
                col += dCol;
            }
        }

        return moves.toArray(new Position[0]);
    }
}