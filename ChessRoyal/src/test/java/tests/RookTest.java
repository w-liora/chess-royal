package tests;

import models.Position;
import models.Square;
import models.Rook;
import models.Pawn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Test;

/*
 * Tests pour la classe Rook
 */

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class RookTest {

	private Square[][] board = {{new Square(new Rook(new Position(0,0),1)), new Square(null), new Square(new Pawn(new Position(0,2),1)), new Square(new Pawn(new Position(0,3),1))},
								{new Square(null), new Square(null), new Square(null), new Square(null)},
								{new Square(null), new Square(null), new Square(null), new Square(null)},
								{new Square(new Pawn(new Position(3,0),0)), new Square(new Rook(new Position(3,1),0)), new Square(new Pawn(new Position(3,2),0)), new Square(new Pawn(new Position(3,3),0))}
								};
	
	@Test
	@DisplayName("Test de la tour")
	@Order(1)
	public void Test() {
		//Test des déplacements possibles initiaux
		Assertions.assertArrayEquals(board[0][0].getPiece().getPossibleMoves(board), new Position[] {new Position(1,0), new Position(2,0), new Position(3,0), new Position(0, 1)});
		Assertions.assertArrayEquals(board[3][1].getPiece().getPossibleMoves(board), new Position[] {new Position(2,1), new Position(1,1), new Position(0, 1)});
		
		//Test de déplacement de tours
		board[0][0].getPiece().moveTo(board,2,0);
		Assertions.assertEquals(null, board[0][0].getPiece());
		Assertions.assertEquals(2, board[2][0].getPiece().getRow());
		Assertions.assertEquals(0, board[2][0].getPiece().getCol());
		board[3][1].getPiece().moveTo(board, 1, 1);
		
		//Second test de déplacement possible
		Assertions.assertArrayEquals(board[2][0].getPiece().getPossibleMoves(board), new Position[] {new Position(1,0), new Position(0,0), new Position(3,0), new Position(2,1), new Position(2,2), new Position(2,3)});
		Assertions.assertArrayEquals(board[1][1].getPiece().getPossibleMoves(board), new Position[] {new Position(0,1), new Position(2,1), new Position(3,1), new Position(1,0), new Position(1,2), new Position(1,3)});
		
		//Second test de déplacement afin de tester les positions précédentes
		board[2][0].getPiece().moveTo(board, 2, 1);

		Assertions.assertEquals(2, board[2][1].getPiece().getPrevRow());
		Assertions.assertEquals(0, board[2][1].getPiece().getPrevCol());
		

	}
	
	
}