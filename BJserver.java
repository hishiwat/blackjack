import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class BJserver {
    public static final int PORT = 8080;
    private static int idCounter = 0;
    private static boolean gameInProgress = false; // ゲーム進行中フラグ
    private static Card cardlist = new Card();
    private static ArrayList<String> dealerCardList = new ArrayList<>();

    // プレイヤーリスト（スレッドセーフ）
    private static List<Player> players = Collections.synchronizedList(new ArrayList<>());
    
    private static boolean showdownCalled = false;

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
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)), true)) {
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
                            // 再ログイン
                            // プレイヤー情報を初期化(チップ数は引継)
                            player.setOnline();
                            player.resetForNewRound();
                            player.setOut(out);
                            break;
                        } else {
                            // 同じ名前の人がオンライン状態
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
                if (gameInProgress) {
                    out.println("Inprogress");
                    player.setState(PlayerState.SPECTATOR);
                } else {
                    out.println("Connected");
                }
                out.println(player.getID());
                out.println(player.getChip());

                // クライアントが END を送るまで待機
                while (true) {
                    String line = in.readLine();
                    if (line == null || line.equals("END")) {
                        player.setOffline();
                        player.setState(PlayerState.LOGOUT);
                        List<Player> activePlayers = getActivePlayers();
                        if (activePlayers.size() == 0) {
                            gameInProgress = false;// ゲームのリセット
                            System.out.println("All players left. Waiting for new connections.");
                            break;
                        }

                        checkStartCondition(); // Ready状態の確認
                        checkAllPlayersBet();// BET状態の確認
                        break;
                    }



                    // 他のプレイヤー情報を要求された場合
                    if (line.equals("GET_OTHER_PLAYERS")) {
                        String otherPlayersInfo = getOtherPlayersInfo(player.getName());
                        out.println("OTHER_PLAYERS_INFO:" + otherPlayersInfo);
                    }

                    // ゲーム開始の応答表示
                    if (line.equals("OK")) {
                        player.setState(PlayerState.READY);
                        if (!checkStartCondition())
                            out.println("Waiting for all players to be ready...");
                    }

                    if (line.startsWith("BET ")) {
                        try {
                            if (line.equals("BET canceled")) {
                                player.setState(PlayerState.SPECTATOR);
                                checkAllPlayersBet();
                                List<Player> activePlayers = getActivePlayers();
                                if (activePlayers.size() == 0) {
                                    gameInProgress = false;// ゲームのリセット
                                    System.out.println("All players have cancelled bets. Quit the game.");
                                    synchronized (players) {
                                        for (Player p : getOnlinePlayers()) {
                                            p.setState(PlayerState.WAITING);
                                            p.sendMessage("GAME_RESET");
                                        }
                                    }
                                    continue;
                                }

                            } else {
                                int betAmount = Integer.parseInt(line.substring(4).trim());
                                if (betAmount > 0 && betAmount <= player.getChip()) {
                                    player.chipBet(betAmount);
                                    player.setState(PlayerState.BET);
                                    out.println("Bet accepted: " + betAmount);

                                    if (!checkAllPlayersBet())
                                        out.println("Waiting for all players to bet...");

                                } else {
                                    out.println("Invalid bet amount. You have " + player.getChip() + " chips.");
                                }
                            }

                        } catch (NumberFormatException e) {
                            out.println("Invalid bet format. Use: BET <amount>");
                        }
                    }

                    if (line.equals("HIT")) {
                        String card = cardlist.getCard();  // ランダムにカードを引く
                        player.addCard(card);              // プレイヤーの手札に追加
                        out.println("HIT_CARD:" + card);   // クライアントにカードを送信

                        int score = calculateScore(player.getCards());
                        if (score > 21) {
                            player.setState(PlayerState.STAND);
                            out.println("BUST");
                            checkShowdown(); // 自動でSTAND扱いとして判定
                        } else {
                            out.println("YOUR_TURN"); // クライアントが再び操作できるようにする
                        }
                        
                        // 他のプレイヤー情報を更新
                        broadcastOtherPlayersInfo();
                    }
                    
                    if (line.equals("STAND")) {
                        player.setState(PlayerState.STAND);
                        sendNextTurn(player); //次の人にターンを移す
                        checkShowdown();
                        
                        // 他のプレイヤー情報を更新
                        broadcastOtherPlayersInfo();
                    }

                    if (line.equals("CONTINUE_YES")) {
                        player.resetForNewRound();
                        player.setState(PlayerState.WAITING);
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

    // 他のプレイヤーの詳細情報を取得
    private static String getOtherPlayersInfo(String currentPlayerName) {
        StringBuilder info = new StringBuilder();
        synchronized (players) {
            for (Player p : players) {
                if (p.getOnlineState() && !p.getName().equals(currentPlayerName)) {
                    info.append(p.getName()).append("|");
                    info.append(p.getChip()).append("|");
                    info.append(p.getBet()).append("|");
                    info.append(p.getState()).append("|");
                    // カード情報を追加（ゲーム中のみ）
                    if (gameInProgress && p.getCards().size() > 0) {
                        for (String card : p.getCards()) {
                            info.append(card).append(",");
                        }
                        if (!p.getCards().isEmpty()) {
                            info.setLength(info.length() - 1); // 最後のカンマを削除
                        }
                    }
                    info.append(";");
                }
            }
        }
        return info.toString();
    }

    //次の人にターンを移す
    private static void sendNextTurn(Player current) {
        List<Player> activePlayers = getActivePlayers();
        int currentIndex = activePlayers.indexOf(current);

        for (int i = currentIndex + 1; i < activePlayers.size(); i++) {
            Player next = activePlayers.get(i);
            if (next.getState() == PlayerState.PLAYING) {
                next.sendMessage("YOUR_TURN");
                return;
            }
        }

    // 残りの誰も PLAYING 状態でなければ勝敗判定
    checkShowdown();
    }


    // オンラインでかつ観戦モード・ログアウトでないプレイヤーのみ
    private static List<Player> getActivePlayers() {
        List<Player> activePlayers = new ArrayList<>();
        synchronized (players) {
            for (Player p : players) {
                if (p.getOnlineState() &&
                        p.getState() != PlayerState.SPECTATOR &&
                        p.getState() != PlayerState.LOGOUT) {
                    activePlayers.add(p);
                }
            }
        }
        return activePlayers;
    }

    private static List<Player> getOnlinePlayers() {
        List<Player> onlinePlayers = new ArrayList<>();
        synchronized (players) {
            for (Player p : players) {
                if (p.getOnlineState())
                    onlinePlayers.add(p);
            }
        }
        return onlinePlayers;
    }

    // activeなプレイヤーの状態がそろっているか確認
    private static boolean allActivePlayersAreInState(PlayerState state) {
        List<Player> activePlayers = getActivePlayers();
        if (activePlayers.isEmpty())
            return false;

        for (Player p : activePlayers) {
            if (p.getState() != state) {
                return false;
            }
        }
        return true;
    }

    private static synchronized boolean checkStartCondition() {
        if (!gameInProgress && allActivePlayersAreInState(PlayerState.READY)) {
            gameInProgress = true;
            System.out.println("=== GAME START ===");
            broadcast("GAME_START");
            return true;
        }
        return false;
    }

    private static synchronized boolean checkAllPlayersBet() {
        if (gameInProgress && allActivePlayersAreInState(PlayerState.BET)) {
            showdownCalled = false;
            dealCards();
            return true;
        }
        return false;
    }
    
    // 全員の行動が完了したかを確認し、一度だけ勝敗判定を呼び出す
    private static synchronized void checkShowdown() {
        // 既に勝敗判定が呼び出されていれば何もしない
        if (showdownCalled) {
            return;
        }

        List<Player> activePlayers = getActivePlayers();
        if (activePlayers.isEmpty()) {
            return;
        }

        // まだ行動中のプレイヤー(PLAYING)がいるか確認
        for (Player p : activePlayers) {
            if (p.getState() == PlayerState.PLAYING) {
                return; // 全員の行動完了を待つ
            }
        }

        // 全員の行動が完了したので、ディーラーのカードを配布してから勝敗判定に移行
        showdownCalled = true;
        dealDealerCards();
    }


    // カード配布
    // 各プレイヤーに2枚ずつ，ディーラは1枚
    // カード配布時にプレイヤーの状態をPLAYINGにする
    private static void dealCards() {
        cardlist.shuffle();

        dealerCardList.clear();
        String card = cardlist.getCard();
        dealerCardList.add(card);

        List<Player> activePlayers = getActivePlayers();
        for (Player p : activePlayers) {
            p.setState(PlayerState.PLAYING); // 状態をPLAYINGに変更
            p.sendMessage("CARDS");
            for (int i = 0; i < 2; i++) {
                card = cardlist.getCard();
                p.setCard(card);
                p.sendMessage("PLAYER_CARD:" + card);

            }
            p.sendMessage("DEALER_CARD:" + dealerCardList.get(0));
        }

        //最初の人のターンにする
        if (!activePlayers.isEmpty()) {
            Player firstPlayer = activePlayers.get(0);
            firstPlayer.sendMessage("YOUR_TURN");
        }

        // 全プレイヤーに他のプレイヤー情報を送信
        broadcastOtherPlayersInfo();
    }

    // ディーラーのカードを配布する
    private static void dealDealerCards() {
        // ディーラーが17以上になるまでカードを引く
        int dealerScore = calculateScore(dealerCardList);
        while (dealerScore < 17) {
            String card = cardlist.getCard();
            dealerCardList.add(card);
            dealerScore = calculateScore(dealerCardList);
        }
        
        // 全プレイヤーにディーラーのカードを1枚ずつ送信（緊迫感を演出）
        for (int i = 1; i < dealerCardList.size(); i++) {
            final int cardIndex = i;
            for (Player p : players) {
                if (p.getOnlineState() && p.getState() != PlayerState.SPECTATOR && p.getState() != PlayerState.LOGOUT) {
                    p.sendMessage("DEALER_CARD:" + dealerCardList.get(cardIndex));
                }
            }
            
            // 各カードの間に1秒の間隔を設ける
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // 1秒待ってから勝敗判定を実行
        new Thread(() -> {
            try {
                Thread.sleep(1000); // 1秒待機
                judgeAndDistribute();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private static void judgeAndDistribute() {
        int dealerScore = calculateScore(dealerCardList);
        boolean dealerBust = dealerScore > 21;

        for (Player p : players) {
            if(p.getState() == PlayerState.SPECTATOR || p.getState() == PlayerState.LOGOUT) {
                continue;
            }

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

            // ゲームに参加したプレイヤーには結果を送信
            if (p.getState() == PlayerState.PLAYING || p.getState() == PlayerState.STAND) {
                 p.sendMessage("GAME_RESULT:" + resultMessage);
                 p.sendMessage("CHIPS:" + p.getChip());
            }
        }
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        // プレイヤーの継続意思を聞く
        askForContinuation();
    }

    // 全てのプレイヤーに継続意思を確認するメッセージを送信
    // 観戦中のプレイヤーには専用のメッセージを送る処理を追加
    private static void askForContinuation() {
        gameInProgress = false; 
        
        List<Player> playersInGame = getActivePlayers();
        List<Player> allOnlinePlayers = new ArrayList<>(getOnlinePlayers());

        for(Player p : allOnlinePlayers){
            if(playersInGame.contains(p)){
                p.sendMessage("CONTINUE?");
            } else if (p.getState() == PlayerState.SPECTATOR) {
				p.setState(PlayerState.WAITING);
                p.sendMessage("SPECTATOR_NEXT_ROUND");
            }
        }
    }

    // 継続しないプレイヤーはオフラインにし、次のラウンドの準備を確認する
    private static synchronized void checkAllPlayersReadyForNextRound() {
        // LOGOUT状態のプレイヤーをオフラインにする
        for (Player p : players) {
            if (p.getState() == PlayerState.LOGOUT) {
                p.setOffline();
            }
        }

        // オンラインのプレイヤーが誰もいなくなったら待機
        if (getOnlinePlayers().isEmpty()) {
            System.out.println("All players left. Waiting for new connections.");
            gameInProgress = false;
            return;
        }

        boolean allReady = true;
        for (Player p : getOnlinePlayers()) {
            // LOGOUT状態のプレイヤーは次のラウンドの準備判定から除外
            if (p.getState() != PlayerState.WAITING && p.getState() != PlayerState.LOGOUT) {
                allReady = false;
                break;
            }
        }

        if (allReady) {
            System.out.println("All remaining players are ready. Starting next round.");
            gameInProgress = false;

            dealerCardList.clear();

            for (Player p : getOnlinePlayers()) {
                p.resetForNewRound();
                p.sendMessage("NEXT_ROUND");
            }
        }
    }

    /**
     * 補助メソッド：手札の点数を計算する
     * * @param cards 手札のリスト
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
     * * @param message 送信するメッセージ
     */
    private static void broadcast(String message) {
        for (Player p : players) {
            p.sendMessage(message);
        }
    }

    /**
     * 補助メソッド：全プレイヤーに他のプレイヤー情報を送信する
     */
    private static void broadcastOtherPlayersInfo() {
        for (Player p : players) {
            if (p.getOnlineState()) {
                String otherPlayersInfo = getOtherPlayersInfo(p.getName());
                p.sendMessage("OTHER_PLAYERS_INFO:" + otherPlayersInfo);
            }
        }
    }
}
