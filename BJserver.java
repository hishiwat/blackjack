import java.io.*;
import java.net.*;
import java.util.*;

public class BJserver {
    public static final int PORT = 8080;
    private static int idCounter = 0;
    private static boolean gameInProgress = false; // ゲーム進行中フラグ

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

    //クライアント処理スレッド
    private static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true)
            ) {
                String name = in.readLine();
                if (name == null || name.equals("END")) return;

                int playerId;
                synchronized (BJserver.class) {
                    playerId = ++idCounter;
                }

                Player player = new Player(name, playerId, out);
                players.add(player);  // リストに追加

                System.out.println("User ID: " + player.getID() + " Name: " + player.getName());
                out.println(player.getID());
                out.println(player.getChip());

                // クライアントが END を送るまで待機
                while (true) {
                    String line = in.readLine();
                    if (line == null || line.equals("END")) break;

                    // プレイヤー一覧を表示したい場合
                    if (line.equals("LIST")) {
                        out.println("=== Player List ===");
                        synchronized (players) {
                            for (Player p : players) {
                                out.println("ID: " + p.getID() + ", Name: " + p.getName());
                            }
                        }
                        out.println("=== End of List ===");
                    }

					// ゲーム開始の応答表示
					if (line.equals("OK")) {
						player.setReady(true);
						boolean allOk = true;
						for (Player p : players) {
							if (!p.isReady()) {
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
}

class Player {
    private String name;
    private int id;
    private int chip;
	private boolean isReady;
	private PrintWriter out;

    public Player(String name, int id, PrintWriter out){
        this.name = name;
        this.id = id;
        this.chip = 500;
		this.isReady = false;
		this.out = out;
    }

    public String getName(){
        return name;
    }

    public int getID(){
        return id;
    }

    public int getChip(){
        return chip;
    }

	public boolean isReady(){
		return isReady;
	}

	public void setReady(boolean isReady){
		this.isReady = isReady;
	}

	public void sendMessage(String message){
		out.println(message);
	}
}
