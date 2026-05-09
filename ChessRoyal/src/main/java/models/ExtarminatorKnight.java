package main.java.models;
import java.util.ArrayList;
import java.lang.Math;

public class ExtarminatorKnight extends Knight {
	//Constructeur
	public ExtarminatorKnight(Position pos, int col) {
		super(pos, col);
		symbol = 'E';
	}
	


	public void moveTo(Square[][] board, int x, int y) {

		int oldX = pos.getRow();
		int oldY = pos.getCol();

		int dx = x - oldX;
		int dy = y - oldY;



		if (Math.abs(dx) == 2 && Math.abs(dy) == 1) {

			int stepX = dx / 2;

			mange_si_piece_adverse(board, oldX + stepX, oldY);
			mange_si_piece_adverse(board, oldX + dx, oldY);
			mange_si_piece_adverse(board, x, y);

		} else if (Math.abs(dx) == 1 && Math.abs(dy) == 2) {

			int stepY = dy / 2;

			mange_si_piece_adverse(board, oldX, oldY + stepY);
			mange_si_piece_adverse(board, oldX, oldY + dy);
			mange_si_piece_adverse(board, x, y);
		}

		board[x][y].setPiece(this);
		board[oldX][oldY].setPiece(null);

		prevPos = pos;
		pos = new Position(x, y);

		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board.length; j++) {
				if (board[i][j] != null && board[i][j].getPiece() != null) {
					board[i][j].getPiece().justMoved = false;
				}
			}
		}

		justMoved = true;
	}

	private void mange_si_piece_adverse(Square[][] board, int nx, int ny) {
		if (board[nx][ny].getPiece() != null &&
			board[nx][ny].getPiece().getColor() != this.getColor()) {
			board[nx][ny].setPiece(null);
		}
	}
}