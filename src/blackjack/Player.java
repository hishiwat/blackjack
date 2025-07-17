package blackjack;

import java.io.PrintWriter;
import java.util.ArrayList;

public class Player {
    private String name;
    private int id;
    private int chip;
    private int bet;
    private PlayerState state;
    private boolean _isOnline;
    private PrintWriter out;
    private ArrayList<String> cardList = new ArrayList<>();
    private static final int RECHARGE_AMOUNT = 100;

    public Player(String name, int id, int chip, PrintWriter out) {
        this.name = name;
        this.id = id;
        this.chip = chip;
        this.bet = 0;
        this.state = PlayerState.WAITING;
        this.out = out;
        this._isOnline = true;
    }

    public String getName() {
        return name;
    }

    public int getID() {
        return id;
    }

    public int getChip() {
        return chip;
    }

    public int getBet() {
        return bet;
    }

    public PlayerState getState() {
        return state;
    }

    public boolean getOnlineState() {
        return _isOnline;
    }

    public void setOnline() {
        _isOnline = true;
    }

    public void setOffline() {
        _isOnline = false;
    }

    public void setState(PlayerState state) {
        this.state = state;
    }

    public void chipBet(int bet) {
        this.bet = bet;
        this.chip -= bet;
    }

    public void setCard(String card) {
        cardList.add(card);
    }

    public ArrayList<String> getCards() {
        return cardList;
    }

    public int getScore() {
        int total = 0;
        int aceCount = 0;

        for (String card : cardList) {
            String rank = card.substring(1);
            switch (rank) {
                case "J":
                case "Q":
                case "K":
                    total += 10;
                    break;
                case "A":
                    total += 11;
                    aceCount++;
                    break;
                default:
                    total += Integer.parseInt(rank);
            }
        }

        while (total > 21 && aceCount > 0) {
            total -= 10;
            aceCount--;
        }

        return total;
    }

    public void addCard(String card) {
        cardList.add(card);
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public void winChips(int amount) {
        this.chip += amount;
    }

    public void setChip(int chip) {
        this.chip = chip;
    }

    public void rechargeChips() {
        if (this.chip <= 0) {
            this.chip = RECHARGE_AMOUNT;
        }
    }

    public void resetForNewRound() {
        this.cardList.clear();
        this.bet = 0;
        this.state = PlayerState.WAITING;
    }

    public void setOut(PrintWriter out) {
        this.out = out;
    }
}
