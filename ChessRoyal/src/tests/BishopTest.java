package tests;

import models.Position;
import models.Square;
import models.Bishop;
import models.Pawn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Test;

/*
 * Tests pour la classe Bishop
 */

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class BishopTest {

	private Square[][] board = {
		{new Square(new Bishop(new Position(0,0),1)), new Square(null), new Square(new Pawn(new Position(0,2),1)), new Square(new Pawn(new Position(0,3),1))},
		{new Square(null), new Square(null), new Square(null), new Square(null)},
		{new Square(null), new Square(null), new Square(null), new Square(null)},
		{new Square(new Pawn(new Position(3,0),0)), new Square(new Pawn(new Position(3,1),0)), new Square(new Bishop(new Position(3,2),0)), new Square(new Pawn(new Position(3,3),0))}
	};

	@Test
	@DisplayName("Test des fous")
	@Order(1)
	public void Test() {
		//Test des déplacements possibles initiaux
		Assertions.assertArrayEquals(board[0][0].getPiece().getPossibleMoves(board), new Position[] {new Position(1,1), new Position(2,2), new Position(3,3)});
		Assertions.assertArrayEquals(board[3][2].getPiece().getPossibleMoves(board), new Position[] {new Position(2,1), new Position(1,0), new Position(2,3)});

		//Test de déplacement de fous
		board[0][0].getPiece().moveTo(board,2,0);
		Assertions.assertEquals(null, board[0][0].getPiece());
		Assertions.assertEquals(2, board[2][0].getPiece().getRow());
		Assertions.assertEquals(0, board[2][0].getPiece().getCol());
		board[3][2].getPiece().moveTo(board, 2, 3);

		//Second test de déplacements possibles
		Assertions.assertArrayEquals(board[2][0].getPiece().getPossibleMoves(board), new Position[] {new Position(1,1), new Position(3,1)});
		Assertions.assertArrayEquals(board[2][3].getPiece().getPossibleMoves(board), new Position[] {new Position(1,2), new Position(0,1), new Position(3,2)});

		//Second test de déplacement afin de tester les positions précédentes
		board[2][3].getPiece().moveTo(board, 1, 2);

		Assertions.assertEquals(2, board[1][2].getPiece().getPrevRow());
		Assertions.assertEquals(3, board[1][2].getPiece().getPrevCol());
	}
}