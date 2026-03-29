package game;
import java.io.Console;

import models.*;

/*
 * Cette classe implémente une version textuelle du jeu d'échec, n'utilisant que les classes associées au pièces.
 * Le plateau s'affiche dans la console à chaque tour, et le joueur choisis une case où se trouve une de ses pièces,
 * puis une case où il souhaite la déplacer. Le jeu continue jusqu'à la capture d'un roi.
 */
public class TextualGame {
	
	//Permet de changer la couleur d'affichage sur la console
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	
	
	//Affichage du plateau, on admet que le dit plateau est un carré
	static void printBoard(Square[][] board) {
		int l = board.length;
		System.out.println(ANSI_BLACK + " "+"_".repeat(6*l-1) + " " + ANSI_RESET);
		for (int i=0; i<l; i++) {
			for (int j=0; j<l; j++) {
				System.out.print(ANSI_BLACK + "|  " + ANSI_RESET);
				Piece piece = board[i][j].getPiece();
				if (piece==null) {System.out.print(" ");}
				else { if (piece.getColor()==0) {
					System.out.print(ANSI_YELLOW + piece.symbol + ANSI_RESET);}
				else {
					System.out.print(ANSI_BLACK + piece.symbol + ANSI_RESET);
				}
				}
				System.out.print("  ");
			}
			System.out.println(ANSI_BLACK + "|" + ANSI_RESET);
			System.out.println(ANSI_BLACK + "|_____".repeat(l)+"|" + ANSI_RESET);
		}
	}
	
	public static void main(String args[]) {
		Square[][] board = {{new Square(new Rook(new Position(0,0),1)), new Square(new Knight(new Position(0,1),1)), new Square(new Bishop(new Position(0,2),1)), new Square(new Queen(new Position(0,3),1)), new Square(new King(new Position(0,4),1)), new Square(new Bishop(new Position(0,5),1)), new Square(new Knight(new Position(0,6),1)), new Square(new Rook(new Position(0,7),1))},
				{new Square(new Pawn(new Position(1,0),1)), new Square(new Pawn(new Position(1,1),1)), new Square(new Pawn(new Position(1,2),1)), new Square(new Pawn(new Position(1,3),1)), new Square(new Pawn(new Position(1,4),1)), new Square(new Pawn(new Position(1,5),1)), new Square(new Pawn(new Position(1,6),1)), new Square(new Pawn(new Position(1,7),1))},
				{new Square(null), new Square(null), new Square(null), new Square(null), new Square(null), new Square(null), new Square(null), new Square(null)},
				{new Square(null), new Square(null), new Square(null), new Square(null), new Square(null), new Square(null), new Square(null), new Square(null)},
				{new Square(null), new Square(null), new Square(null), new Square(null), new Square(null), new Square(null), new Square(null), new Square(null)},
				{new Square(null), new Square(null), new Square(null), new Square(null), new Square(null), new Square(null), new Square(null), new Square(null)},
				{new Square(new Pawn(new Position(6,0),0)), new Square(new Pawn(new Position(6,1),0)), new Square(new Pawn(new Position(6,2),0)), new Square(new Pawn(new Position(6,3),0)), new Square(new Pawn(new Position(6,4),0)), new Square(new Pawn(new Position(6,5),0)), new Square(new Pawn(new Position(6,6),0)), new Square(new Pawn(new Position(6,7),0))},
				{new Square(new Rook(new Position(7,0),0)), new Square(new Knight(new Position(7,1),0)), new Square(new Bishop(new Position(7,2),0)), new Square(new Queen(new Position(7,3),0)), new Square(new King(new Position(7,4),0)), new Square(new Bishop(new Position(7,5),0)), new Square(new Knight(new Position(7,6),0)), new Square(new Rook(new Position(7,7),0))}
				};
		printBoard(board);
	}
}
