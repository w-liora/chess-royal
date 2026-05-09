package main.java.messages;

import java.io.Serializable;

/**
 * Message transportant un coup d'échecs entre client et serveur.
 *
 * Le serveur reçoit ce message du joueur actif et le rediffuse
 * (via {@code envoyerATous}) à tous les clients pour qu'ils
 * mettent à jour leur plateau local.
 *
 * La promotion est encodée par le champ {@code promotionSymbol} :
 *   'Q' = dame, 'R' = tour, 'B' = fou, 'N' = cavalier, '\0' = pas de promotion.
 */
public class CoupEchecs implements Serializable {

    private final int fromRow;
    private final int fromCol;
    private final int toRow;
    private final int toCol;
    /** '\0' si pas de promotion, sinon 'Q', 'R', 'B' ou 'N'. */
    private final char promotionSymbol;

    public CoupEchecs(int fromRow, int fromCol, int toRow, int toCol) {
        this(fromRow, fromCol, toRow, toCol, '\0');
    }

    public CoupEchecs(int fromRow, int fromCol, int toRow, int toCol, char promotionSymbol) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow   = toRow;
        this.toCol   = toCol;
        this.promotionSymbol = promotionSymbol;
    }

    public int getFromRow()           { return fromRow; }
    public int getFromCol()           { return fromCol; }
    public int getToRow()             { return toRow; }
    public int getToCol()             { return toCol; }
    public char getPromotionSymbol()  { return promotionSymbol; }
    public boolean isPromotion()      { return promotionSymbol != '\0'; }

    @Override
    public String toString() {
        return "CoupEchecs[" + fromRow + "," + fromCol
                + " -> " + toRow + "," + toCol
                + (isPromotion() ? " promo=" + promotionSymbol : "") + "]";
    }
}