package tests;

import models.Position;
import models.Square;
import models.Pawn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Test;

/*
 * Tests pour la classe Pawn
 */

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class PawnTest {

	private Square[][] board = {{new Square(new Pawn(new Position(0,0),1)), null, new Square(new Pawn(new Position(0,2),1)), new Square(new Pawn(new Position(0,3),1))},
								{new Square(null), new Square(null), new Square(null), new Square(null)},
								{new Square(null), new Square(null), new Square(null), new Square(null)},
								{new Square(new Pawn(new Position(3,0),0)), new Square(new Pawn(new Position(3,1),0)), new Square(new Pawn(new Position(3,2),0)), new Square(new Pawn(new Position(3,3),0))}
	};
	
	@Test
	@DisplayName("Test des pions")
	@Order(1)
	public void Test() {
		//Test des déplacements possibles initiaux
		Assertions.assertArrayEquals(board[0][0].getPiece().getPossibleMoves(board), new Position[] {new Position(1,0), new Position(2,0)});
		Assertions.assertArrayEquals(board[3][2].getPiece().getPossibleMoves(board), new Position[] {new Position(2,2), new Position(1,2)});
		
		//Test de déplacement de pions
		board[0][0].getPiece().moveTo(new Position(2,0));
		board[2][0]=board[0][0];
		board[0][0]=null;
		Assertions.assertEquals(2, board[2][0].getPiece().getRow());
		Assertions.assertEquals(0, board[2][0].getPiece().getCol());
		board[3][1].getPiece().moveTo(new Position(2,1));
		board[2][1] = board[3][1];
		board[3][1] = null;
		
		//Second test de déplacment possibles, incluant un en passant
		Assertions.assertArrayEquals(board[2][0].getPiece().getPossibleMoves(board), new Position[] {});
		Assertions.assertArrayEquals(board[2][1].getPiece().getPossibleMoves(board), new Position[] {new Position(1,1), new Position(1,0)});
		
		//Second test de déplacement afin de tester les posiions précédentes
		board[2][1].getPiece().moveTo(new Position(1,1));
		board[1][1]=board[2][1];
		board[2][1]=null;
		Assertions.assertEquals(2, board[1][1].getPiece().getPrevRow());
		Assertions.assertEquals(1, board[1][1].getPiece().getPrevCol());
	}
	
	
}
