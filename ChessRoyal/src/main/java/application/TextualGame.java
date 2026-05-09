package main.java.application;

import java.util.Scanner;
import main.java.models.*;

/**
 * Cette classe implémente une version textuelle du jeu d'échec, n'utilisant que les classes associées au pièces.
 * Le plateau s'affiche dans la console à chaque tour, et le joueur choisis une case où se trouve une de ses pièces,
 * puis une case où il souhaite la déplacer. Le jeu continue jusqu'à la capture d'un roi.
 */

public class TextualGame {
	
	//Permet de changer la couleur d'affichage sur la console
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	
	
	/**Affichage du plateau, on admet que le plateau est un carré*/
	static void printBoard(Square[][] board) {
		int l = board.length;
		System.out.println(ANSI_BLACK + "   "+"_".repeat(6*l-1) + " " + ANSI_RESET);
		for (int i=0; i<l; i++) {
			System.out.print(8-i + " ");
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
			System.out.println(ANSI_BLACK + "  " + "|_____".repeat(l)+"|" + ANSI_RESET);
		}
		System.out.println("     a     b     c     d     e     f     g     h");
	}
	
	/**
	 * Convertis une case donnée en notation échiquéenne (type "b5") en une position sur le plateau.
	 *Si le texte ne correspond pas à une case, l'une des deux coordonnées sera égale à -1.
	*/
	static Position squareToPos(String square) {
		char row = ' ';
		char column = ' ';
		/*S'il n'y a pas au moins deux caractères dans la chaine, il y aurait un problème d'exécution,
		/n le résout en ne modifiant row et column que si il y a des éléments à lire. On s'assure par ailleurs
		que la longueur est exactement la bonne.*/
		if (square.length() == 2) {
			row = square.charAt(1);
			column = square.charAt(0);
		}
		int x=-1;
		int y=-1;
		switch (row) {
		case '8' -> x=0;
		case '7' -> x=1;
		case '6' -> x=2;
		case '5' -> x=3;
		case '4' -> x=4;
		case '3' -> x=5;
		case '2' -> x=6;
		case '1' -> x=7;
		}
		switch (column) {
		case 'a' -> y=0;
		case 'b' -> y=1;
		case 'c' -> y=2;
		case 'd' -> y=3;
		case 'e' -> y=4;
		case 'f' -> y=5;
		case 'g' -> y=6;
		case 'h' -> y=7;
		}
		return new Position(x,y);
	}
	
	/**Vérifie si une position est contenue dans un tableau de positions */
	//Utilisé lorsqu'un joueur choisis une case vers laquelle se déplacer.
	static boolean contains(Position[] tab, Position pos) {
		for (Position p: tab) {
			if (p.equals(pos)) {
				return true;
			}
		}
		return false;
	}
	public static void main(String args[]) {
		//Le plateau de départ
		Square[][] board = {{new Square(new Rook(new Position(0,0),1)), new Square(new Knight(new Position(0,1),1)), new Square(new Bishop(new Position(0,2),1)), new Square(new Queen(new Position(0,3),1)), new Square(new King(new Position(0,4),1)), new Square(new Bishop(new Position(0,5),1)), new Square(new Knight(new Position(0,6),1)), new Square(new Rook(new Position(0,7),1))},
				{new Square(new Pawn(new Position(1,0),1)), new Square(new Pawn(new Position(1,1),1)), new Square(new Pawn(new Position(1,2),1)), new Square(new Pawn(new Position(1,3),1)), new Square(new Pawn(new Position(1,4),1)), new Square(new Pawn(new Position(1,5),1)), new Square(new Pawn(new Position(1,6),1)), new Square(new Pawn(new Position(1,7),1))},
				{new Square(null), new Square(null), new Square(null), new Square(null), new Square(null), new Square(null), new Square(null), new Square(null)},
				{new Square(null), new Square(null), new Square(null), new Square(null), new Square(null), new Square(null), new Square(null), new Square(null)},
				{new Square(null), new Square(null), new Square(null), new Square(null), new Square(null), new Square(null), new Square(null), new Square(null)},
				{new Square(null), new Square(null), new Square(null), new Square(null), new Square(null), new Square(null), new Square(null), new Square(null)},
				{new Square(new Pawn(new Position(6,0),0)), new Square(new Pawn(new Position(6,1),0)), new Square(new Pawn(new Position(6,2),0)), new Square(new Pawn(new Position(6,3),0)), new Square(new Pawn(new Position(6,4),0)), new Square(new Pawn(new Position(6,5),0)), new Square(new Pawn(new Position(6,6),0)), new Square(new Pawn(new Position(6,7),0))},
				{new Square(new Rook(new Position(7,0),0)), new Square(new Knight(new Position(7,1),0)), new Square(new Bishop(new Position(7,2),0)), new Square(new Queen(new Position(7,3),0)), new Square(new King(new Position(7,4),0)), new Square(new Bishop(new Position(7,5),0)), new Square(new Knight(new Position(7,6),0)), new Square(new Rook(new Position(7,7),0))}
				};
		
		int player = 0; //indique le joueur dont c'est le tour. 0 pour les blancs, 1 pour les noirs.
		Scanner inputReader = new Scanner(System.in); //Utilisé pour lire les entrées du joueur.
		boolean fin = false; //Indique si la partie est finie
		
		//boucle de jeu
		do {
			//On affiche le plateau
			printBoard(board);
			
			//Indique le joueur qui doit jouer
			if (player==0) {System.out.println("Au tour du joueur blanc.");}
			else {System.out.println("Au tour du joueur noir.");}
			
			//On demande ensuite au joueur de donner une case d'où se déplacer. 
			//Si la case est invalide, il faudra recommencer, c'est le but de cette boucle
			Piece chosenPiece = null; //La pièce choisie
			boolean isValid = false;
			do { 
				System.out.println("Choisissez la pièce à déplacer.");
				String start = inputReader.nextLine();
				Position pos = squareToPos(start);
				int row = pos.getRow(); //La ligne de la case choisie
				int col = pos.getCol(); //La colonne de la case choisie
				//ces variables sont initialisées à -1 par squareToPos si le texte donné n'est pas une case.
				if (row==-1 || col==-1) {
					System.out.println("Le texte rentré n'est pas une case, recommencez.");
				}
				//Sinon, on peut accéder à la pièce choisie. On vérifie que la case contient une pièce, qu'elle est de la bonne couleur, et qu'elle peut bouger.
				else {
					chosenPiece = board[row][col].getPiece();
					if (chosenPiece == null) {
						System.out.println("Cette case ne contient pas de pièce, recommencez.");
					}
					else if (chosenPiece.getColor() != player) {
						System.out.println("Cette pièce appartient à l'autre joueur, recommencez.");
					}
					else if (chosenPiece.getPossibleMoves(board).length==0) {
						System.out.println("Cette pièce ne peut pas se déplacer, recommencez.");
					}
					//Une fois toutes ces vérifications effectuées, on sait que la pièce choisie est correcte. On sort alors de la boucle.
					else {
						isValid = true;
					}
				}
			} while (! isValid);
			
			Position[] possibleMoves = chosenPiece.getPossibleMoves(board); //Liste des coups possibles
			//On demande maintenant au jour où il souhaite aller. 
			//Encore une fois, il faudra vérifier que la case est correcte, ce qui est fait en testant une appartenance à possibleMoves.
			isValid = false;
			int endRow=0;
			int endCol=0;
			
			do {
				System.out.println("Choisissez la case vers laquelle se déplacer.");
				String end = inputReader.nextLine();
				Position pos = squareToPos(end);
				if (contains(possibleMoves, pos)) {
					isValid = true;
					endRow= pos.getRow(); //On récupère la ligne de la case choisie
					endCol = pos.getCol(); //et la colonne
				}
				else {
					System.out.println("le texte rentré n'est pas une case valide, recommencez.");
				}
			} while (! isValid);
			
			//Finalement, on s'occupe de déplacer la pièce choisie. Si elle capture un roi, la partie se termine, sinon c'est au joueur suivant.
			if (board[endRow][endCol].getPiece() instanceof King) {
				fin = true;
			}
			chosenPiece.moveTo(board, endRow, endCol);
			if (player==0) {
				player =1;
			}
			else {
				player=0;
			}
		} while (! fin);
		
		//Fin de la partie
		//Comme player est modifié à la fin de la boucle de tour, cette variable indique le joueur perdant en fin de partie.
		if (player == 0) {
			System.out.println("Fin de partie. Victoire du joueur noir.");
		}
		else {
			System.out.println("Fin de partie. Victoire du joueur blanc.");
		}
		
		inputReader.close();
	}
}
