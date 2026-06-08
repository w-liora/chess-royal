package tests;

import models.Position;
import models.Square;
import models.Rook;
import models.LavaSquare;
import models.Pawn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Test;

/*
 * Tests pour la case de lave 
 */

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class LavaSquareTest {

	private Square[][] board = {{new Square(new Rook(new Position(0,0),1)), new Square(null), new Square(new Pawn(new Position(0,2),1)), new Square(new Pawn(new Position(0,3),1))},
								{new Square(null), new Square(null), new Square(null), new Square(null)},
								{new LavaSquare(), new Square(null), new Square(null), new Square(null)},
								{new Square(new Pawn(new Position(3,0),0)), new Square(new Rook(new Position(3,1),0)), new Square(new Pawn(new Position(3,2),0)), new Square(new Pawn(new Position(3,3),0))}
								};
	
	@Test
	@DisplayName("Test de la case de lave")
	@Order(1)
	public void Test() {
		//On déplace une pièce sur la case de lave et on vérifie qu'elle disparait
		board[3][0].getPiece().moveTo(board,2,0);
		Assertions.assertNull(board[2][0].getPiece()); //La pièce disparait car sur la case de lave
	}	
}