package messages;

import java.io.Serializable;

///Pour transporter 3 infos : id de la carte (NUmCard), row et int = case ou on applique la carte
/// 
public class CardMessage implements Serializable {

    private static final long serialVersionUID = 1L;

   
    private final int NumCard; //Pour identifier la carte dans chessboardctrl
    private final int row;
    private final int col;

    public CardMessage(int NumCard, int row, int col) {
        this.NumCard = NumCard ;
        this.row = row;
        this.col = col;
    }

    public int getNumCard() { return NumCard; }
    public int getRow()              { return row; }
    public int getCol()              { return col; }

    @Override
    public String toString() {
        return "CardMessage[type=" + NumCard + " -> (" + row + "," + col + ")]";
    }
}