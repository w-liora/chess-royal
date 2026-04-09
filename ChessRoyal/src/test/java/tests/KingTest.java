package tests;

import models.Position;
import models.Square;
import models.King;
import models.Rook;
import models.Pawn;
import models.Knight;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Test;

/*
 * Tests pour la classe King
 */

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class KingTest {

	private Square[][] board = {{new Square(new Rook(new Position(0,0),1)), new Square(null), new Square(null),new Square(null), new Square(new King(new Position(0,4),1))},
								{new Square(null), new Square(null), new Square(null), new Square(null), new Square(null)},
								{new Square(null), new Square(null),new Square(null),  new Square(new Pawn(new Position(2,3),0)), new Square(new Pawn(new Position(2, 4),0))},
								{new Square(new Rook(new Position(3,0),0)),new Square(null),  new Square(null), new Square(new Knight(new Position(3,3),0)), new Square(new King(new Position(3,4),0))}
								};
	
	@Test
	@DisplayName("Test du roi")
	@Order(1)
	public void Test() {
		//Test des déplacements possibles initiaux
		Assertions.assertArrayEquals(board[0][4].getPiece().getPossibleMoves(board), new Position[] {new Position(0,3), new Position(1,3), new Position(1,4),new Position(0,2) });
		Assertions.assertArrayEquals(board[3][4].getPiece().getPossibleMoves(board), new Position[] {});
		
		//Test de déplacement de rois : test du roque
		board[0][4].getPiece().moveTo(board,0,2);
		Assertions.assertEquals(null, board[0][4].getPiece());
		Assertions.assertEquals(0, board[0][2].getPiece().getRow());
		Assertions.assertEquals(2, board[0][2].getPiece().getCol());
		//On vérifie la tour 
		Assertions.assertEquals(0, board[0][2].getPiece().getRow());
		Assertions.assertEquals(2, board[0][2].getPiece().getCol());
		board[0][2].getPiece().moveTo(board, 1, 2);
		
		//Second test de déplacement possible
		Assertions.assertArrayEquals(board[1][2].getPiece().getPossibleMoves(board), new Position[] {new Position(0,1),  new Position(0,2), new Position(1,1), new Position(1,3), new Position(2,1),  new Position(2,2), new Position(2,3)});
		
		//Second test de déplacement afin de tester les positions précédentes
		board[1][2].getPiece().moveTo(board, 2, 2);

		Assertions.assertEquals(1, board[2][2].getPiece().getPrevRow());
		Assertions.assertEquals(2, board[2][2].getPiece().getPrevCol());
		

	}
	
	
}