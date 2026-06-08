package tests;

import models.Position;
import models.Square;
import models.Pawn;
import models.ExterminatorKnight;
import models.Queen;
import models.Rook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Test;

/*
 * Tests pour la classe ExterminatorKnight
 */

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class ExterminatorKnightTest {

	private Square[][] board = {{new Square(null), new Square(null), new Square(null), new Square(new Queen(new Position(0,3),0))},
								{new Square(null), new Square(null), new Square(new ExterminatorKnight(new Position(1,2),1)), new Square(null)},
								{new Square(new Pawn(new Position(2,0),1)), new Square(null), new Square(new Rook(new Position(2,2),1)), new Square(new Queen(new Position(2,3),1))},
								{new Square(null), new Square(new ExterminatorKnight(new Position(3,1),0)), new Square(new Pawn(new Position(3,2),0)), new Square(new Pawn(new Position(3,3),1))}
	};
	
	@Test
	@DisplayName("Test des cavaliers exterminateurs")
	@Order(1)
	public void Test() {
		//Test des déplacements possibles initiaux
		Assertions.assertArrayEquals(board[3][1].getPiece().getPossibleMoves(board), new Position[] {new Position(1,0), new Position(1,2), new Position(2,3)});
		Assertions.assertArrayEquals(board[1][2].getPiece().getPossibleMoves(board), new Position[] {new Position(3,1),  new Position(0,0)});
		
		//Test de déplacement des cavaliers
		board[1][2].getPiece().moveTo(board,0,0);
		Assertions.assertEquals(null, board[1][2].getPiece());
		Assertions.assertEquals(0, board[0][0].getPiece().getRow());
		Assertions.assertEquals(0, board[0][0].getPiece().getCol());
		
		
		
		//On vérifie que si le cavalier extermianteur passe au dessus d'une pièce (de couleur adverse), il la fait disparaitre
		board[3][1].getPiece().moveTo(board, 2,3);
		Assertions.assertEquals(null, board[3][1].getPiece());
		Assertions.assertNotNull(board[3][2].getPiece()); //Car le cavalierexterminateur mange pas le pion (car couleur pas adverse) se situant sur cette case
		Assertions.assertEquals(null, board[3][3].getPiece()); //Car le cavalierexterminateur mange le pion (de couleur adverse) se situant sur cette case
		

		
	}
	
	
}
