package blackjack;

import java.util.Random;

public class Card {
    private String[][] cardlist;
    private int[] index;
    private int count;

    public Card() {
        this.cardlist = new String[][] {
                { "HA", "H2", "H3", "H4", "H5", "H6", "H7", "H8", "H9", "H10", "HJ", "HQ", "HK" },
                { "SA", "S2", "S3", "S4", "S5", "S6", "S7", "S8", "S9", "S10", "SJ", "SQ", "SK" },
                { "CA", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9", "C10", "CJ", "CQ", "CK" },
                { "DA", "D2", "D3", "D4", "D5", "D6", "D7", "D8", "D9", "D10", "DJ", "DQ", "DK" }
        };
        this.index = new int[52];
        for (int i = 0; i < 52; i++) {
            this.index[i] = i;
        }
        this.count = 0;
    }

    public String getCard() {
        if (count >= 52) {
            count = 0; // カードがない場合リセット
            return getCard();
        }
        int i = index[count]; // 番号の取り出し
        count++;
        return cardlist[i / 13][i % 13];
    }

    public void shuffle() {
        Random rand = new Random();
        for (int i = 51; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int temp = index[i];
            index[i] = index[j];
            index[j] = temp;
        }
        count = 0;
    }

    public void initialize() {
        count = 0;
    }

}
