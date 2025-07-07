import java.io.*;
import java.net.*;
import java.util.*;
import java.security.*;
import java.util.Base64.*;

public class BJserver {
    public static final int PORT = 8080;
    private static int idCounter = 0;
    private static boolean gameInProgress = false; // ゲーム進行中フラグ
    private static Card cardlist = new Card();
    private static ArrayList<String> dealerCardList = new ArrayList<>();

    // プレイヤーリスト（スレッドセーフ）
    private static List<Player> players = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Blackjack WebSocket server started on port " + PORT);

        try {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Connection accepted: " + socket);
                new Thread(new ClientHandler(socket)).start();
            }
        } finally {
            serverSocket.close();
        }
    }

    // クライアント処理スレッド
    private static class ClientHandler implements Runnable {
        private Socket socket;
        private Transmit transmit;
        private Player player; // プレイヤー参照を保持

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (
                InputStream is = socket.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(is));
                OutputStream os = socket.getOutputStream();
            ) {
                transmit = new Transmit(in, os, is);
                
                String name;
                int playerId;
                
                // プレイヤー名の受信
                name = transmit.readMessage();
                if (name == null) return;
                
                this.player = getPlayerByName(name);
                if (this.player != null) {
                    if (this.player.getOnlineState()) {
                        // 既にオンラインの場合は名前が使用中
                        transmit.sendMessage("UsedName");
                        return;
                    } else {
                        // オフラインの場合は既存プレイヤーを再利用
                        this.player.setTransmit(transmit);
                    }
                } else {
                    // 新しいプレイヤーを作成
                    synchronized (BJserver.class) {
                        playerId = ++idCounter;
                    }
                    this.player = new Player(name, playerId, 500, transmit);
                    players.add(this.player);
                }
                
                this.player.setOnline();
                System.out.println("User ID: " + this.player.getID() + " Name: " + this.player.getName());
                transmit.sendMessage("ID:" + this.player.getID());
                transmit.sendMessage("CHIP:" + this.player.getChip());

                // メッセージループ
                String line;
                while ((line = transmit.readMessage()) != null) {
					System.out.println(line);
                    if (line.equals("END")) {
                        player.setOffline();
                        System.out.println(player.getOnlineState());
                        break;
                    }

                    // プレイヤー一覧を表示したい場合
                    if (line.equals("LIST")) {
						String message = "LIST,";
                        synchronized (players) {
                            for (Player p : players) {
                                if (p.getOnlineState())
                                    message += "ID: " + p.getID() + ", Name: " + p.getName() + ", chip: " + p.getChip() + ",";
                            }
                        }
						transmit.sendMessage(message);
                    }

                    // ゲーム開始の応答表示
                    if (line.equals("OK")) {
                        player.setState(PlayerState.READY);
                        boolean allOk = true;
                        for (Player p : players) {
                            // オンラインの人がREADY状態でないとき
                            if (p.getState() != PlayerState.READY && p.getOnlineState()) {
                                allOk = false;
                                break;
                            }
                        }
                        if (allOk && !gameInProgress) {
                            // ゲーム開始状態に移行
                            gameInProgress = true;
                            System.out.println("=== GAME START ===");
                            for (Player p : players) {
                                p.sendMessage("Game Start");
                            }

                        } else {
                            transmit.sendMessage("Waiting for all players to be ready...");
                        }
                    }

                    if (line.startsWith("BET ")) {
                        try {
                            // BET <amount>で送信される。amountをintに変換
                            int betAmount = Integer.parseInt(line.substring(4).trim());
                            if (betAmount > 0 && betAmount <= this.player.getChip()) {
                                this.player.chipBet(betAmount);
                                this.player.setState(PlayerState.BET);
                                transmit.sendMessage("Bet accepted: " + betAmount);

                                boolean allBet = true;
                                for (Player p : players) {
                                    if (p.getState() != PlayerState.BET && p.getOnlineState()) {
                                        allBet = false;
                                        break;
                                    }
                                }

                                synchronized (BJserver.class) {
                                    if (allBet && gameInProgress) {
                                        // カード配布
                                        dealCards();
										System.out.println("Cards dealt");
                                    } else {
                                        transmit.sendMessage("Waiting for all players to bet...");
                                    }
                                }

                            } else {
                                transmit.sendMessage("Invalid bet amount. You have " + this.player.getChip() + " chips.");
                            }

                        } catch (NumberFormatException e) {
                            transmit.sendMessage("Invalid bet format. Use: BET <amount>");
                        }
                    }

                    if (line.equals("HIT")) {
                        // カードをもう一枚引く処理
                        // <追加>
                    }

                    if (line.equals("STAND")) {
                        // 全員がSTAND状態になったら次に進む
                        // <追加>

                        // ディーラのカード処理
                        // <追加>

                        // 勝敗判定に移行
                        judgeAndDistribute();
                    }

                    if (line.equals("CONTINUE_YES")) {
                        this.player.resetForNewRound();
                        this.player.setState(PlayerState.READY);
                        checkAllPlayersReadyForNextRound();
                    }

                    if (line.equals("CONTINUE_NO")) {
                        this.player.setState(PlayerState.LOGOUT);
                        // 他のプレイヤーを待たずに次のステップへ進む
                        checkAllPlayersReadyForNextRound();
                        break; // このプレイヤーのループを抜けて接続を終了する
                    }
                }

            } catch (IOException e) {
                System.err.println("Connection error: " + e);
            } finally {
                // プレイヤーをオフライン状態にする
                if (this.player != null) {
                    this.player.setOffline();
                    System.out.println("Player " + this.player.getName() + " went offline");
                }
                try {
                    socket.close();
                    System.out.println("Connection closed: " + socket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // WebSocket通信クラス
    static class Transmit {
        BufferedReader in;
        OutputStream os;
        InputStream is;

        Transmit(BufferedReader in, OutputStream os, InputStream is) throws IOException {
            this.in = in;
            this.os = os;
            this.is = is;
            handShake();
        }

        private void handShake() throws IOException {
            try {
                String key = null;
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("Sec-WebSocket-Key: ")) {
                        key = line.substring(19).trim();
                    }
                    if (line.isEmpty()) {
                        break;
                    }
                }
                if (key == null) {
                    throw new IOException("Sec-WebSocket-Key not found.");
                }
                
                String acceptKey = key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                byte[] sha1 = md.digest(acceptKey.getBytes("UTF-8"));
                String acceptKeyBase64 = Base64.getEncoder().encodeToString(sha1);

                String response = 
                        "HTTP/1.1 101 Switching Protocols\r\n" +
                        "Upgrade: websocket\r\n" +
                        "Connection: Upgrade\r\n" +
                        "Sec-WebSocket-Accept: " + acceptKeyBase64 + "\r\n\r\n";
                os.write(response.getBytes("UTF-8"));
                os.flush();
            } catch (Exception e) {
                System.err.println("Handshake error: " + e);
            }
        }

        String readMessage() throws IOException {
            int b1 = is.read();
            int b2 = is.read();
            if (b1 != 0x81) {
                return null;
            }

            boolean masked = (b2 & 0x80) != 0;
            int payloadLength = b2 & 0x7F;
            if (payloadLength == 126) {
                int b3 = is.read();
                int b4 = is.read();
                payloadLength = (b3 << 8) | b4;
            } else if (payloadLength == 127) {
                throw new IOException("Payload length too long.");
            }

            byte[] mask = null;
            if (masked) {
                mask = new byte[4];
                is.read(mask, 0, 4);
            }

            byte[] payload = new byte[payloadLength];
            is.read(payload, 0, payloadLength);

            if (masked) {
                for (int i = 0; i < payloadLength; i++) {
                    payload[i] ^= mask[i % 4];
                }
            }

            return new String(payload, "UTF-8");
        }

        void sendMessage(String message) throws IOException {
            byte[] payload = message.getBytes("UTF-8");
            int payloadLength = payload.length;
			System.out.println("sendMessage: " + message);

            os.write(0x81);
            if (payloadLength <= 125) {
                os.write(payloadLength);
            } else if (payloadLength <= 65535) {
                os.write(0x7E);
                os.write(new byte[] {(byte) (payloadLength >> 8), (byte) (payloadLength & 0xFF)});
            } else {
                throw new IOException("Payload too long.");
            }
            
            os.write(payload);
            os.flush();
        }
    }

    // プレイヤーnameが使用済みか調べる
    public static Player getPlayerByName(String name) {
        for (Player p : players) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    private static void dealCards() {
        cardlist.shuffle();
        String card = cardlist.getCard();
        dealerCardList.add(card);

        for (Player p : players) {
			String message = "Cards,";
            for (int i = 0; i < 2; i++) {
                card = cardlist.getCard();
                p.setCard(card);
                message += card + ",";

            }
			p.sendMessage(message);
            p.sendMessage("Dealer Card," + dealerCardList.get(0));
        }
    }

    private static void judgeAndDistribute() {
        int dealerScore = calculateScore(dealerCardList);
        boolean dealerBust = dealerScore > 21;

        if (dealerBust) {
            broadcast("Dealer Busted!");
        }

        for (Player p : players) {
            int playerScore = calculateScore(p.getCards());
            boolean playerBust = playerScore > 21;
            String resultMessage;

            if (playerBust) {
                // プレイヤーのバースト: ベットは没収 (ベット時に引かれているので何もしない)
                resultMessage = "You Busted! You lose " + p.getBet() + " chips.";
            } else if (dealerBust || playerScore > dealerScore) {
                // プレイヤーの勝利
                // ブラックジャック(2枚で21)の場合は2.5倍、それ以外は2倍
                int winnings = (playerScore == 21 && p.getCards().size() == 2) ? (int) (p.getBet() * 2.5)
                        : (p.getBet() * 2);
                p.winChips(winnings);
                resultMessage = "You Win! You get " + winnings + " chips.";
            } else if (playerScore == dealerScore) {
                // 引き分け
                p.winChips(p.getBet()); // ベット額がそのまま戻る
                resultMessage = "Push (Draw). Your bet of " + p.getBet() + " is returned.";
            } else {
                // プレイヤーの負け
                resultMessage = "You Lose. You lose " + p.getBet() + " chips.";
            }
            p.sendMessage(resultMessage);
            p.sendMessage("Your total chips: " + p.getChip());
        }

        // プレイヤーの継続意思を聞く
        askForContinuation();
    }

    // 全てのプレイヤーに継続意思を確認するメッセージを送信
    private static void askForContinuation() {
        gameInProgress = false; // ゲーム自体は一旦終了
        broadcast("CONTINUE?");
    }

    // 継続しないプレイヤーをリストから削除し、次のラウンドの準備を確認する
    private static synchronized void checkAllPlayersReadyForNextRound() {
        // LOGOUT状態のプレイヤーがいればリストから削除
        players.removeIf(p -> p.getState() == PlayerState.LOGOUT);

        if (players.isEmpty()) {
            System.out.println("All players left. Waiting for new connections.");
            gameInProgress = false;
            return;
        }

        // 残ったプレイヤー全員が次のラウンドの準備ができているか確認
        for (Player p : players) {
            if (p.getState() != PlayerState.READY) {
                return; // まだ意思表示していないプレイヤーがいる
            }
        }

        // 全員が継続を選択したら、次のゲームを開始する
        System.out.println("All remaining players are ready. Starting next round.");
        // 既存のゲーム開始ロジックを呼び出す
        // checkAllPlayersReady();
    }

    /**
     * 補助メソッド：手札の点数を計算する
     * 
     * @param cards 手札のリスト
     * @return 点数
     */
    private static int calculateScore(ArrayList<String> cards) {
        int score = 0;
        int aceCount = 0;
        for (String card : cards) {
            String cardNum = card.substring(1);// 数字部分を抜き出し H2 -> 2
            if (cardNum.equals("A")) {
                aceCount++;
                score += 11;
            } else if ("JQK10".contains(cardNum)) {
                score += 10;
            } else {
                score += Integer.parseInt(cardNum);
            }
        }
        while (score > 21 && aceCount > 0) {
            score -= 10;
            aceCount--;
        }
        return score;
    }

    /**
     * 補助メソッド：全プレイヤーにメッセージを送信する
     * 
     * @param message 送信するメッセージ
     */
    private static void broadcast(String message) {
        for (Player p : players) {
            p.sendMessage(message);
        }
    }
}
