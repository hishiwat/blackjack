import java.io.*;
import java.net.*;
import java.util.*;

public class BJserver {
    public static final int PORT = 8080;
    private static int idCounter = 0;
    private static boolean gameInProgress = false; // ゲーム進行中フラグ
    private static String cardlist[] = new String[] { "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q",
            "K" };
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
                String name = in.readLine();
                if (name == null || name.equals("END"))
                    return;

                int playerId;
                synchronized (BJserver.class) {
                    playerId = ++idCounter;
                }

                Player player = new Player(name, playerId, 500, out);
                players.add(player); // リストに追加

                System.out.println("User ID: " + player.getID() + " Name: " + player.getName());
                out.println(player.getID());
                out.println(player.getChip());

                // クライアントが END を送るまで待機
                while (true) {
                    String line = in.readLine();
                    if (line == null || line.equals("END"))
                        break;

                    // プレイヤー一覧を表示したい場合
                    if (line.equals("LIST")) {
                        out.println("=== Player List ===");
                        synchronized (players) {
                            for (Player p : players) {
                                out.println("ID: " + p.getID() + ", Name: " + p.getName() + ", chip: " + p.getChip());
                            }
                        }
                        out.println("=== End of List ===");
                    }

                    // ゲーム開始の応答表示
                    if (line.equals("OK")) {
                        player.setState(PlayerState.READY);
                        boolean allOk = true;
                        for (Player p : players) {
                            if (p.getState() != PlayerState.READY) {
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
                            out.println("Waiting for all players to be ready...");
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
                                    if (p.getState() != PlayerState.BET) {
                                        allBet = false;
                                        break;
                                    }
                                }

                                synchronized (BJserver.class) {
                                    if (allBet && gameInProgress) {
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

    private static void dealCards() {
        String card = cardlist[10];
        dealerCardList.add(card);

        for (Player p : players) {
            p.sendMessage("Cards");
            for (int i = 0; i < 2; i++) {
                card = cardlist[i];
                p.setCard(card);
                p.sendMessage(card);

            }
            p.sendMessage("Dealer Card " + dealerCardList.get(0));
        }
    }

}
