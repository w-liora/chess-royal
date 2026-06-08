package tests;

import models.Position;
import models.Square;
import models.PawnKamikaze;
import models.Pawn;
import models.Knight;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Test;

/*
 * Tests pour la classe Pawn Kamikaze
 */

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class PawnKamikazeTest {

	private Square[][] board = {{new Square(new PawnKamikaze(new Position(0,0),1)), new Square(null), new Square(null), new Square(null)},
								{new Square(new Pawn(new Position(1,0),0)), new Square(null),new Square(new Knight(new Position(1,2),1)), new Square(null)},
								{new Square(new Pawn(new Position(2,0),0)), new Square(null), new Square(new Knight(new Position(2,2),1)), new Square(null)},
								{new Square(null), new Square(new PawnKamikaze(new Position(3,1),0)), new Square(null), new Square(null)}
	};
	
	@Test
	@DisplayName("Test des pions kamikaze")
	@Order(1)
	public void Test() {
		//Test des déplacements possibles initiaux
		Assertions.assertArrayEquals(board[0][0].getPiece().getPossibleMoves(board), new Position[] {});
		Assertions.assertArrayEquals(board[3][1].getPiece().getPossibleMoves(board), new Position[] {new Position(2,1),new Position(2,2),new Position(1,1)});
		
		//Test d'explosion d'un pion kamikaze
		board[3][1].getPiece().moveTo(board,2,1);
		Assertions.assertNotNull(board[2][1].getPiece()); //Le pion n'a pas encore explosé
		board[2][1].getPiece().moveTo(board,1,1); //Le pion explose
		Assertions.assertEquals(null, board[2][0].getPiece());
		Assertions.assertEquals(null, board[1][0].getPiece());
		Assertions.assertEquals(null, board[0][0].getPiece());
		Assertions.assertEquals(null, board[1][1].getPiece()); //Le pion a explsoé donc n'est plus là
		Assertions.assertEquals(null, board[1][2].getPiece());
		Assertions.assertEquals(null, board[2][2].getPiece());

	}
	
	
}
