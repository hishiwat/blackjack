import java.io.*;
import java.net.*;
import java.util.*;

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
        System.out.println("Server started on port " + PORT);

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

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(
                            new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true)) {
                String name;
                int playerId;
                Player player;
                while (true) {
                    name = in.readLine();

                    if (name == null || name.equals("END"))
                        return;
                    player = getPlayerByName(name);
                    if (player != null) {
                        if (!player.getOnlineState()) {
                            player.setOnline();
                            player.resetForNewRound();
                            player.setOut(out);
                            break;
                        } else {
                            out.println("UsedName");
                        }

                    } else {
                        synchronized (BJserver.class) {
                            playerId = ++idCounter;
                        }

                        player = new Player(name, playerId, 500, out);
                        players.add(player); // リストに追加

                        break;
                    }
                }

                System.out.println("User ID: " + player.getID() + " Name: " + player.getName());
                out.println(player.getID());
                out.println(player.getChip());

                // クライアントが END を送るまで待機
                while (true) {
                    String line = in.readLine();
                    if (line == null || line.equals("END")) {
                        player.setOffline();
                        // System.out.println(player.getOnlineState());
                        break;
                    }

                    // プレイヤー一覧を表示したい場合
                    if (line.equals("LIST")) {
                        out.println("=== Player List ===");
                        synchronized (players) {
                            for (Player p : players) {
                                if (p.getOnlineState())
                                    out.println(
                                            "ID: " + p.getID() + ", Name: " + p.getName() + ", chip: " + p.getChip());
                            }
                        }
                        out.println("=== End of List ===");
                    }

                    // ゲーム開始の応答表示
                    if (line.equals("OK")) {
                        player.setState(PlayerState.READY);

                        synchronized (BJserver.class) {
                            boolean allOk = true;
                            for (Player p : players) {
                                if (p.getState() != PlayerState.READY && p.getOnlineState()) {
                                    allOk = false;
                                    break;
                                }
                            }

                            if (allOk && !gameInProgress) {
                                gameInProgress = true;
                                System.out.println("=== GAME START ===");

                                for (Player p : players) {
                                    p.sendMessage("Game Start");
                                }
                            } else {
                                out.println("Waiting for all players to be ready...");
                            }
                        }
                    }

                    if (line.startsWith("BET ")) {
                        try {
                            // BET <amount>で送信される。amountをintに変換
                            int betAmount = Integer.parseInt(line.substring(4).trim());
                            if (betAmount > 0 && betAmount <= player.getChip()) {
                                player.chipBet(betAmount);
                                player.setState(PlayerState.BET);
                                out.println("Bet accepted: " + betAmount);

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
                                    } else {
                                        out.println("Waiting for all players to bet...");
                                    }
                                }

                            } else {
                                out.println("Invalid bet amount. You have " + player.getChip() + " chips.");
                            }

                        } catch (NumberFormatException e) {
                            out.println("Invalid bet format. Use: BET <amount>");
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
                        player.resetForNewRound();
                        player.setState(PlayerState.READY);
                        checkAllPlayersReadyForNextRound();
                    }

                    if (line.equals("CONTINUE_NO")) {
                        player.setState(PlayerState.LOGOUT);
                        // 他のプレイヤーを待たずに次のステップへ進む
                        checkAllPlayersReadyForNextRound();
                        break; // このプレイヤーのループを抜けて接続を終了する
                    }
                }

            } catch (IOException e) {
                System.err.println("Connection error: " + e);
            } finally {
                try {
                    socket.close();
                    System.out.println("Connection closed: " + socket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
        dealerCardList.clear();
        String card = cardlist.getCard();
        dealerCardList.add(card);

        for (Player p : players) {
            p.sendMessage("Cards");
            for (int i = 0; i < 2; i++) {
                card = cardlist.getCard();
                p.setCard(card);
                p.sendMessage(card);

            }
            p.sendMessage("Dealer Card " + dealerCardList.get(0));
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
            if (p.getState() != PlayerState.READY && p.getOnlineState()) {
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
