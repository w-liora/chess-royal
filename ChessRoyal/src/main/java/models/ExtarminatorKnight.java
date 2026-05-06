package models;
import java.util.ArrayList;
import java.lang.Math;

public class ExtarminatorKnight extends Knight {
	//Constructeur
	public ExtarminatorKnight(Position pos, int col) {
		super(pos, col);
		symbol = 'E';
	}
	
	public void moveTo(Square[][] board, int x, int y) {
		//Modifie la plateau correctement
		board[x][y].setPiece(this);
		board[pos.getRow()][pos.getCol()].setPiece(null);
		//Change la position de la pièce
		Position newPos = new Position(x,y);
		prevPos = pos;
		pos = newPos;
		//Cette pièce est maintenant la dernière à s'être déplacée
		int l = board.length;
		for (int i=0; i<l; i++) {
			for (int j=0; j<l; j++) {
				if (board[i][j] != null && board[i][j].getPiece()!=null) {
					board[i][j].getPiece().justMoved = false;
				}
			}
		}
		justMoved = true;
	}

}
