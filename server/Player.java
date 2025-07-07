import java.io.PrintWriter;
import java.util.ArrayList;

enum PlayerState {
    WAITING, // 初期状態
    READY, // 「OK」を送信した状態
    BET, // ベットを完了した状態
    PLAYING, // ヒット/スタンドを選択できる状態
    STAND, // スタンドを選択した or バーストした状態
    LOGOUT, // 継続しないことを選択した状態
    SPECTATER,// 観戦状態
    // ここに他の状態を追加していく
}

public class Player {
    private String name;
    private int id;
    private int chip;
    private int bet;
    private PlayerState state;
    private boolean _isOnline;
    private BJserver.Transmit transmit;
    private ArrayList<String> cardList = new ArrayList<>();

    public Player(String name, int id, int chip, BJserver.Transmit transmit) {
        this.name = name;
        this.id = id;
        this.chip = chip;
        this.bet = 0;
        this.state = PlayerState.WAITING;
        this.transmit = transmit;
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

    public void setTransmit(BJserver.Transmit transmit) {
        this.transmit = transmit;
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

    public void sendMessage(String message) {
        try {
            transmit.sendMessage(message);
        } catch (Exception e) {
            System.err.println("Error sending message to " + name + ": " + e.getMessage());
        }
    }

    public void winChips(int amount) {
        this.chip += amount;
    }

    public void resetForNewRound() {
        this.cardList.clear();
        this.bet = 0;
        this.state = PlayerState.WAITING;
    }
}
