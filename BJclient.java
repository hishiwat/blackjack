import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class BJclient {
    private JFrame frame;
    private JTextArea messageArea;
    private JTextField inputField;
    private JButton sendButton;
    private JButton readyButton;
    private JButton listButton;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;
    private String playerName;
    private int playerID;
    private int chip;
    private Player player;
    private ArrayList<String> dealerCardList = new ArrayList<>();
    private boolean isConnected = false;

    // GUI更新用にラベルをフィールドとして宣言
    private JLabel nameLabel;
    private JLabel idLabel;
    private JLabel chipLabel;

    //HITSTAND処理で追加
    private JButton hitButton;
    private JButton standButton;

    public BJclient() {
        createGUI();

        // 名前入力ダイアログ
        showNameDialog();

        connectToServer();
    }

    private void createGUI() {
        frame = new JFrame("Blackjack Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);
        frame.setLocationRelativeTo(null);

        // メインパネル
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // プレイヤー情報パネル
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Player Info"));
        nameLabel = new JLabel("Name: ");
        idLabel = new JLabel("ID: ");
        chipLabel = new JLabel("Chip: ");
        infoPanel.add(nameLabel);
        infoPanel.add(idLabel);
        infoPanel.add(chipLabel);
        mainPanel.add(infoPanel, BorderLayout.NORTH);

        // メッセージエリア
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Messages"));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // ボタンパネル
        JPanel buttonPanel = new JPanel(new FlowLayout());
        readyButton = new JButton("Ready (OK)");
        listButton = new JButton("Show Players (LIST)");
        JButton disconnectButton = new JButton("Disconnect (END)");
        
        //HITSTANDで追加
        hitButton = new JButton("Hit");
        standButton = new JButton("Stand");

        readyButton.setEnabled(false);
        listButton.setEnabled(false);

        //HITSTANDで追加
        hitButton.setEnabled(false);
        standButton.setEnabled(false);

        buttonPanel.add(readyButton);
        buttonPanel.add(listButton);
        buttonPanel.add(disconnectButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);

        //HITSTANDで追加
        buttonPanel.add(hitButton);
        buttonPanel.add(standButton);

        // イベントリスナー
        readyButton.addActionListener(e -> sendReady());
        listButton.addActionListener(e -> sendList());
        disconnectButton.addActionListener(e -> disconnect());

        // アクションリスナ追加
        hitButton.addActionListener(e -> onHit());
        standButton.addActionListener(e -> onStand());
    }

    private void updateInfoPanel() {
        nameLabel.setText("Name: " + this.playerName);
        idLabel.setText("ID: " + this.playerID);
        chipLabel.setText("Chip: " + this.chip);
    }

    private void showNameDialog() {
        String name = JOptionPane.showInputDialog(frame, "Enter your name:", "Player Name", JOptionPane.PLAIN_MESSAGE);
        if (name != null && !name.trim().isEmpty()) {
            playerName = name.trim();
        } else {
            System.exit(0);
        }
    }

    private void connectToServer() {
        try {
            int port = BJserver.PORT;
            InetAddress addr = InetAddress.getByName("localhost");

            addMessage("Connecting to server at " + addr + ":" + port);

            socket = new Socket(addr, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)), true);

            // 名前を送信
            while (true) {
                out.println(playerName);
                String response = in.readLine();
                if (response.equals("UsedName")) {
                    JOptionPane.showMessageDialog(frame, "This name is already in use. Please enter a different name",
                            "Name Conflict",
                            JOptionPane.WARNING_MESSAGE);
                    showNameDialog(); // 新しい名前を取得
                } else {
                    // 有効な名前で接続成功
                    playerID = Integer.parseInt(in.readLine());
                    chip = Integer.parseInt(in.readLine());
                    player = new Player(playerName, playerID, chip, out);
                    addMessage("Connected successfully!");
                    // ゲーム途中の参加の場合は観戦モード
                    if (response.equals("Inprogress")) {
                        addMessage("You are in spectator mode");
                        player.setState(PlayerState.SPECTATOR);
                    }

                    break;
                }
            }

            addMessage("Your ID: " + playerID);
            addMessage("Your chip: " + chip);

            isConnected = true;
            readyButton.setEnabled(true);
            listButton.setEnabled(true);

            // サーバーからのメッセージ受信用スレッド
            Thread readerThread = new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        final String message = line;
                        System.out.println("[DEBUG] Received: " + message); // デバッグ用 受信したメッセージを標準出力に表示
                        SwingUtilities.invokeLater(() -> addMessage(message));

                        //クライアント側のチップの増減を実装
                        if (message.startsWith("Your total chips: ")) {
                            try {
                                String newChipStr = message.substring("Your total chips: ".length()).trim();
                                int newChip = Integer.parseInt(newChipStr);
                                this.chip = newChip;
                                player.winChips(newChip - player.getChip());
                                SwingUtilities.invokeLater(this::updateInfoPanel);
                            } catch (NumberFormatException e) {
                                // エラー処理
                            }
                        } else if (message.equals("Game Start")) {
                            game();
                        }
                        if (message.equals("Game Reset")) {
                            player.resetForNewRound();
                            readyButton.setEnabled(true);
                        }

                        if (message.equals("Cards")) {
                            // 2枚のカードを受け取りリストに格納
                            for (int i = 0; i < 2; i++) {
                                String card = in.readLine();
                                player.setCard(card);
                                SwingUtilities.invokeLater(() -> addMessage(card));
                            }
                            // デバッグ用 リストに格納されているかの確認
                            // for (String c : player.getCards()) {
                            // System.out.println(c);
                            // }

                            line = in.readLine();
                            if (line != null && line.startsWith("Dealer Card ")) {
                                dealerCardList.clear();
                                String dealerCard = line.substring("Dealer Card ".length()).trim();
                                dealerCardList.add(dealerCard);
                                SwingUtilities.invokeLater(() -> addMessage("Dealer's Card: " + dealerCard));
                            }

                            // HITandSTAND処理
                            //hit_stand();
                        }

                        //  ここに追加
                        // if (message.equals("文字列")){操作()}
                        if (message.startsWith("HIT_CARD:")) {
                            String card = message.substring("HIT_CARD:".length());
                            SwingUtilities.invokeLater(() -> {
                                addMessage("You drew: " + card);
                                player.addCard(card);
                            });
                        }

                        if (message.equals("BURST")) {
                            SwingUtilities.invokeLater(() -> {
                                addMessage("You busted!");
                                hitButton.setEnabled(false);
                                standButton.setEnabled(false);
                            });
                        }

                        if (message.equals("YOUR_TURN")) {
                            SwingUtilities.invokeLater(() -> {
                                hitButton.setEnabled(true);
                                standButton.setEnabled(true);
                            });
                        }   


                        if (message.equals("ROUND_END")) {
                            addMessage("Round has ended. Please wait for the next round.");
                        }


                        // サーバーからの継続確認メッセージを受け取る
                        if (message.equals("CONTINUE?")) {
                            // ユーザーに継続するかどうかを尋ねるダイアログを表示
                            askToContinue();
                        }
                        if (message.equals("SPECTATOR_NEXT_ROUND")) {
                            player.resetForNewRound();
                            addMessage("The previous game has ended. You can join the next round.");
                            readyButton.setEnabled(true);
                        }
                    }
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> addMessage("Connection closed by server."));
                }
            });
            readerThread.setDaemon(true);
            readerThread.start();

        } catch (IOException e) {
            addMessage("Connection error: " + e.getMessage());
            JOptionPane.showMessageDialog(frame, "Failed to connect to server: " + e.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void game() {
        chip = player.getChip();
        while (true) {
            String input = JOptionPane.showInputDialog(frame,
                    "Enter chips to bet (you have " + chip + "):",
                    "Bet",
                    JOptionPane.PLAIN_MESSAGE);

            if (input == null) {
                // ベットしなかった場合は観戦モードに移行
                addMessage("Bet canceled.\nSwitch to spectator mode");
                out.println("BET canceled");
                return;
            }

            try {
                int bet = Integer.parseInt(input.trim());

                if (bet <= 0) {
                    JOptionPane.showMessageDialog(frame, "Bet must be more than 0.", "Invalid Bet",
                            JOptionPane.WARNING_MESSAGE);
                } else if (bet > chip) {
                    JOptionPane.showMessageDialog(frame, "You cannot bet more than your chips (" + chip + ").",
                            "Invalid Bet", JOptionPane.WARNING_MESSAGE);
                } else {
                    out.println("BET " + bet);
                    addMessage("You bet: " + bet);
                    player.chipBet(bet);
                    break;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Please enter a valid number.", "Invalid Input",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void sendReady() {
        if (isConnected) {
            out.println("OK");
            addMessage("Sent: OK (Ready)");
            readyButton.setEnabled(false);
        }
    }

    private void sendList() {
        if (isConnected) {
            out.println("LIST");
            addMessage("Sent: LIST");
        }
    }

    private void disconnect() {
        if (isConnected) {
            out.println("END");
            addMessage("Sent: END");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            isConnected = false;
            readyButton.setEnabled(false);
            listButton.setEnabled(false);
        }
        System.exit(0);
    }

    private void addMessage(String message) {
        messageArea.append(message + "\n");
        messageArea.setCaretPosition(messageArea.getDocument().getLength());
    }

    public void show() {
        frame.setVisible(true);
    }

    /*public void hit_stand() {
         <追加>
         カードをもう一枚引く場合はHIT，もう引かない場合はSTAND
        out.println("STAND");
    }*/

    /*onHit()とonStand()に分けてイベント駆動的に処理することで
    ボタンを使った操作が便利になるようにしています*/
        public void onHit() {
            out.println("HIT");
            addMessage("Sent: HIT");
            hitButton.setEnabled(false);
            standButton.setEnabled(false);
        }

    public void onStand() {
        out.println("STAND");
        addMessage("You chose to stand.");

        //ボタンを無効化
        hitButton.setEnabled(false);
        standButton.setEnabled(false);
        }


    // ユーザーにゲームを継続するか聞き、結果をサーバーに送信する
    private void askToContinue() {
        SwingUtilities.invokeLater(() -> {
            int choice = JOptionPane.showConfirmDialog(
                    frame,
                    "Play again?",
                    "Continue",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                // 「はい」が選択されたら、サーバーに"CONTINUE_YES"を送信
                out.println("CONTINUE_YES");
                addMessage("Sent: CONTINUE_YES");
                // 次のラウンドに備えてReadyボタンを有効化する
                readyButton.setEnabled(true);

            } else {
                // 「いいえ」またはダイアログが閉じられたら、"CONTINUE_NO"を送信
                out.println("CONTINUE_NO");
                addMessage("Sent: CONTINUE_NO");
                // クライアント側からも切断処理を呼び出す
                disconnect();
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new BJclient().show();
        });
    }
}
