package models;

public class LavaSquare extends Square {
	
	
	public LavaSquare() {
		piece=null;
	}
	
	
	@Override
	public void effect() {
		piece = null;
	}
	
	
}
