package tests;

import models.Position;
import models.Square;
import models.BouncingBishop;
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

public class BouncingBishopTest {

	private Square[][] board = {
		{new Square(new BouncingBishop(new Position(0,0),1)), new Square(null), new Square(new Pawn(new Position(0,2),1)), new Square(new Pawn(new Position(0,3),1))},
		{new Square(new Pawn(new Position(1,0),0)), new Square(null), new Square(null), new Square(null)},
		{new Square(null), new Square(null), new Square(null), new Square(null)},
		{new Square(new Pawn(new Position(3,0),0)), new Square(new Pawn(new Position(3,1),0)), new Square(new BouncingBishop(new Position(3,2),0)), new Square(new Pawn(new Position(3,3),0))}
	};

	@Test
	@DisplayName("Test des fous rebondissants")
	@Order(1)
	public void Test() {
		//Test des déplacements possibles initiaux
		Assertions.assertArrayEquals(board[0][0].getPiece().getPossibleMoves(board), new Position[] {new Position(1,1), new Position(2,2), new Position(3,3)});
		Assertions.assertArrayEquals(board[3][2].getPiece().getPossibleMoves(board), new Position[] {new Position(2,1), new Position(2, 3), new Position(1,2) , new Position(0,1)});//


	}
}