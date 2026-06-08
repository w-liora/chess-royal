
package models;
import java.io.Serializable;
import models.Square;

public abstract class Card implements Serializable {
	String nom;
	public boolean click_legal;
	
    protected Card(String nom) {
        this.nom = nom;
    }

    public String getNom()         { return nom; }

    public abstract void apply(Square[][] board, int row, int col);
}


