package main.java.models;

import java.io.Serializable;
import main.java.models.Square;

public abstract class Card implements Serializable {
	String nom;
	
    protected Card(String nom) {
        this.nom = nom;
    }

    public String getNom()         { return nom; }

    public abstract void apply(Square[][] board, int row, int col);
}


