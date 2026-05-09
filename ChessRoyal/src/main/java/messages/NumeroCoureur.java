package main.java.messages;

import java.io.Serializable;

public class NumeroCoureur implements Serializable {
	private int numeroCoureur;

	public NumeroCoureur(int numeroCoureur) {
		super();
		this.numeroCoureur = numeroCoureur;
	}

	public int getNumeroCoureur() {
		return numeroCoureur;
	}

	public void setNumeroCoureur(int numeroCoureur) {
		this.numeroCoureur = numeroCoureur;
	}

}
