package tests;

import models.Position;
import models.Square;
import models.Pawn;
import models.Knight;
import models.Queen;
import models.Rook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Test;

/*
 * Tests pour la classe Knight
 */

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class KnightTest {

	private Square[][] board = {{new Square(null), new Square(null), new Square(null), new Square(new Queen(new Position(0,3),0))},
								{new Square(null), new Square(null), new Square(new Knight(new Position(1,2),1)), new Square(null)},
								{new Square(new Pawn(new Position(2,0),1)), null, new Square(new Rook(new Position(2,2),1)), new Square(new Queen(new Position(2,3),1))},
								{new Square(null), new Square(new Knight(new Position(3,1),0)), new Square(null), new Square(new Pawn(new Position(3,3),0))}
	};
	
	@Test
	@DisplayName("Test des cavaliers")
	@Order(1)
	public void Test() {
		//Test des déplacements possibles initiaux
		Assertions.assertArrayEquals(board[3][1].getPiece().getPossibleMoves(board), new Position[] {new Position(1,0), new Position(1,2), new Position(2,3)});
		Assertions.assertArrayEquals(board[1][2].getPiece().getPossibleMoves(board), new Position[] {new Position(3,1), new Position(3,3), new Position(0,0)});
		
		//Test de déplacement des caliers
		board[1][2].getPiece().moveTo(board,0,0);
		Assertions.assertEquals(null, board[1][2].getPiece());
		Assertions.assertEquals(0, board[0][0].getPiece().getRow());
		Assertions.assertEquals(0, board[0][0].getPiece().getCol());
		board[3][1].getPiece().moveTo(board,1, 2);
		
		//Second test de déplacment où le cavalier blanc mange le noir
		board[1][2].getPiece().moveTo(board,0,0);
		Assertions.assertEquals(null, board[1][2].getPiece());
		Assertions.assertEquals(0, board[0][0].getPiece().getRow());
		Assertions.assertEquals(0, board[0][0].getPiece().getCol());
		Assertions.assertEquals(0, board[0][0].getPiece().getColor());
		
		//Second test de déplacement possible où le cavalier blanc est bloqué
		board[0][3].getPiece().moveTo(board, 1, 2);
		Assertions.assertArrayEquals(board[0][0].getPiece().getPossibleMoves(board), new Position[] {});
		
	}
	
	
}
