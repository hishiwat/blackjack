import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class BJclient {
    private JFrame frame;
    private JTextArea messageArea;
    private JButton readyButton;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;
    private String playerName;
    private int playerID;
    private int chip;
    private Player player;
    private ArrayList<String> dealerCardList = new ArrayList<>();
    private boolean isConnected = false;

    // 新しいGUI要素
    private GamePanel gamePanel;
    private boolean cardsProcessing = false; // カード処理中フラグ
    private JLabel playerInfoLabel; // プレイヤー情報表示用ラベル

    public BJclient() {
        showNameDialog();
        createGUI();
        connectToServer();
    }

    private void createGUI() {
        frame = new JFrame("Blackjack");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);

        // 背景色
        Color casinoGreen = new Color(34, 139, 34);
        frame.getContentPane().setBackground(casinoGreen);

        // メインパネル
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(casinoGreen);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 上部情報パネル
        createTopInfoPanel(mainPanel);

        // 中央ゲームエリア
        gamePanel = new GamePanel();
        mainPanel.add(gamePanel.getGamePanel(), BorderLayout.CENTER);

        // 下部コントロールパネル
        createControlPanel(mainPanel);

        frame.add(mainPanel);
    }

    // 上部情報パネルを作成
    private void createTopInfoPanel(JPanel mainPanel) {
        Color casinoGreen = new Color(34, 139, 34);
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(casinoGreen);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        // プレイヤー情報
        playerInfoLabel = new JLabel(
                "Name: " + (playerName != null ? playerName : "") + " | ID: " + playerID + " | Chips: " + chip);
        playerInfoLabel.setForeground(Color.WHITE);
        playerInfoLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topPanel.add(playerInfoLabel, BorderLayout.WEST);

        mainPanel.add(topPanel, BorderLayout.NORTH);
    }

    // コントロールパネルを作成
    private void createControlPanel(JPanel mainPanel) {
        Color casinoGreen = new Color(34, 139, 34);
        JPanel controlPanel = new JPanel(new BorderLayout(10, 10));
        controlPanel.setBackground(casinoGreen);
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        // メッセージエリア
        messageArea = new JTextArea(5, 40);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setBackground(new Color(245, 245, 245));
        messageArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Game Messages"));
        controlPanel.add(scrollPane, BorderLayout.CENTER);

        // ボタンパネル
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setBackground(casinoGreen);

        readyButton = new JButton("Ready");
        JButton disconnectButton = new JButton("Disconnect");

        // ボタンのスタイル設定
        Color buttonColor = new Color(70, 130, 180);
        readyButton.setBackground(buttonColor);
        readyButton.setForeground(Color.WHITE);
        disconnectButton.setBackground(new Color(220, 20, 60));
        disconnectButton.setForeground(Color.WHITE);

        readyButton.setEnabled(false);

        buttonPanel.add(readyButton);
        buttonPanel.add(disconnectButton);

        controlPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        // イベントリスナー
        readyButton.addActionListener(e -> sendReady());
        disconnectButton.addActionListener(e -> disconnect());
    }

    private void createBetPanel() {
        gamePanel.createBetPanel();
        gamePanel.getBetButton().addActionListener(e -> placeBet());
        frame.revalidate();
        frame.repaint();
    }

    private void createContinuePanel() {
        gamePanel.createContinuePanel();
        gamePanel.getContinueYesButton().addActionListener(e -> sendContinueYes());
        gamePanel.getContinueNoButton().addActionListener(e -> sendContinueNo());
        frame.revalidate();
        frame.repaint();
    }

    private void updatePlayerInfo() {
        if (playerInfoLabel != null) {
            playerInfoLabel.setText(
                    "Name: " + (playerName != null ? playerName : "") + " | ID: " + playerID + " | Chips: " + chip);
        }
    }

    private void placeBet() {
        try {
            int bet = Integer.parseInt(gamePanel.getBetField().getText().trim());
            if (bet <= 0) {
                addMessage("Bet must be more than 0.");
                return;
            }
            if (bet > chip) {
                addMessage("You cannot bet more than your chips (" + chip + ").");
                return;
            }

            out.println("BET " + bet);
            player.chipBet(bet);
            chip = player.getChip(); // チップ情報を更新
            updatePlayerInfo();

            // ベットパネルを非表示
            gamePanel.removePanel(gamePanel.getBetPanel());
            frame.revalidate();
            frame.repaint();

        } catch (NumberFormatException ex) {
            addMessage("Please enter a valid number.");
        }
    }

    private void sendContinueYes() {
        out.println("CONTINUE_YES");
        readyButton.setEnabled(true);
        gamePanel.removePanel(gamePanel.getContinuePanel());
        gamePanel.clearDealerCards();
        gamePanel.clearPlayerCards();
        gamePanel.updateDealerScore(-1);
        gamePanel.updatePlayerScore(-1);
        player.resetForNewRound();
        dealerCardList.clear();
        addMessage("Next round is starting. Please press 'Ready' to join the game.");
        frame.revalidate();
        frame.repaint();
    }

    private void sendContinueNo() {
        out.println("CONTINUE_NO");
        disconnect();
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
            out = new PrintWriter(
                    new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)), true);

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
                    SwingUtilities.invokeLater(() -> updatePlayerInfo());
                    // ゲーム途中の参加の場合は観戦モード
                    if (response.equals("Inprogress")) {
                        addMessage("You are in spectator mode");
                        player.setState(PlayerState.SPECTATOR);
                        readyButton.setEnabled(false);
                    } else {
                        addMessage("Please press 'Ready' to join the game.");
                        readyButton.setEnabled(true);
                    }

                    break;
                }
            }

            isConnected = true;

            // 定期的に他のプレイヤー情報を要求するタイマーを開始
            startOtherPlayersUpdateTimer();

            // サーバーからのメッセージ受信用スレッド
            Thread readerThread = new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        final String message = line;

                        // クライアント側のチップの増減を実装
                        if (message.startsWith("Your total chips: ")) {
                            String newChipStr = message.substring("Your total chips: ".length()).trim();
                            int newChip = Integer.parseInt(newChipStr);
                            this.chip = newChip;
                            player.winChips(newChip - player.getChip());
                            SwingUtilities.invokeLater(() -> {
                                updatePlayerInfo();
                            });
                        } else if (message.equals("GAME_START")) {
                            SwingUtilities.invokeLater(() -> {
                                createBetPanel();
                                addMessage("Game started! Please place your bet.");
                                // 他のプレイヤー情報を要求
                                out.println("GET_OTHER_PLAYERS");
                            });
                        }
                        if (message.equals("GAME_RESET")) {
                            player.resetForNewRound();
                            readyButton.setEnabled(true);
                        }

                        if (message.equals("RECHARGED")) {
                            player.rechargeChips();
                            chip = player.getChip();
                            SwingUtilities.invokeLater(this::updatePlayerInfo);
                            addMessage("Your chips have been recharged!");
                        }

                        if (message.equals("CARDS") && !cardsProcessing) {
                            cardsProcessing = true; // フラグを設定
                            try {
                                // 2枚のカードを受け取りリストに格納
                                ArrayList<String> playerCards = new ArrayList<>();

                                for (int i = 0; i < 2; i++) {
                                    String card = in.readLine();
                                    if (card.startsWith("PLAYER_CARD:")) {
                                        card = card.substring("PLAYER_CARD:".length());
                                    }
                                    playerCards.add(card);
                                }

                                // ディーラーのカード
                                String dealerLine = in.readLine();
                                String dealerCard = dealerLine.substring("DEALER_CARD:".length()).trim();

                                // すべてのカードを収集した後、UIを更新
                                final ArrayList<String> finalPlayerCards = playerCards;
                                final String finalDealerCard = dealerCard;

                                SwingUtilities.invokeLater(() -> {
                                    // プレイヤーのカードをクリアして表示
                                    gamePanel.clearPlayerCards();

                                    // プレイヤーのカードを表示
                                    for (String card : finalPlayerCards) {
                                        player.setCard(card);
                                        gamePanel.addPlayerCard(card);
                                    }

                                    // プレイヤーのスコアを更新
                                    int playerScore = player.getScore();
                                    gamePanel.updatePlayerScore(playerScore);

                                    // ディーラーのカードを表示
                                    if (finalDealerCard != null) {
                                        dealerCardList.add(finalDealerCard);
                                        gamePanel.clearDealerCards();
                                        gamePanel.addDealerCard(finalDealerCard);
                                    }

                                    // HIT/STANDボタンを表示
                                    gamePanel.createGameActionPanel();
                                    gamePanel.getHitButton().addActionListener(e -> onHit());
                                    gamePanel.getStandButton().addActionListener(e -> onStand());
                                    addMessage("Choose HIT or STAND.");
                                    frame.revalidate();
                                    frame.repaint();
                                });

                            } catch (IOException e) {
                                addMessage("Failed to process cards: " + e.getMessage());
                                e.printStackTrace();
                            } finally {
                                cardsProcessing = false; // フラグをリセット
                            }
                        }

                        // HIT後のカード追加処理
                        if (message.startsWith("HIT_CARD:")) {
                            String cardCode = message.substring("HIT_CARD:".length());
                            addCardToPlayer(cardCode);
                        }

                        // バースト処理
                        if (message.equals("BUST")) {
                            SwingUtilities.invokeLater(() -> {
                                gamePanel.getHitButton().setEnabled(false);
                                gamePanel.getStandButton().setEnabled(false);
                            });
                        }

                        // ターン開始
                        if (message.equals("YOUR_TURN")) {
                            SwingUtilities.invokeLater(() -> {
                                gamePanel.getHitButton().setEnabled(true);
                                gamePanel.getStandButton().setEnabled(true);
                            });
                        }

                        // 他のプレイヤーの詳細情報
                        if (message.startsWith("OTHER_PLAYERS_INFO:")) {
                            String otherPlayersInfo = message.substring("OTHER_PLAYERS_INFO:".length());
                            SwingUtilities.invokeLater(() -> {
                                gamePanel.updateOtherPlayersDetailed(otherPlayersInfo, playerName);
                                frame.revalidate();
                                frame.repaint();
                            });
                        }

                        // ディーラーのカード追加
                        if (message.startsWith("DEALER_CARD:")) {
                            String cardCode = message.substring("DEALER_CARD:".length());
                            SwingUtilities.invokeLater(() -> {
                                addCardToDealer(cardCode);
                            });
                        }

                        // ゲーム結果（サーバーからの直接メッセージ）
                        if (message.startsWith("GAME_RESULT:")) {
                            String resultMessage = message.substring("GAME_RESULT:".length());
                            SwingUtilities.invokeLater(() -> {

                                // hitとstandボタンを無効化
                                gamePanel.getHitButton().setEnabled(false);
                                gamePanel.getStandButton().setEnabled(false);

                                // 結果をGUIに反映
                                int chipChange = 0;
                                if (resultMessage.startsWith("You Win!")) {
                                    chipChange = Integer.parseInt(resultMessage
                                            .substring("You Win! You get ".length(), resultMessage.indexOf(" chips."))
                                            .trim()); // 勝利時のチップ増加
                                } else if (resultMessage.startsWith("Push (Draw)")) {
                                    chipChange = player.getBet(); // 引き分け
                                } else if (message.startsWith("You Busted!") || message.startsWith("You Lose")) {
                                    chipChange = 0;
                                }

                                player.winChips(chipChange);
                                chip = player.getChip(); // ローカル変数chipも更新
                                updatePlayerInfo(); // GUI反映

                                gamePanel.createResultPanel(resultMessage, chipChange - player.getBet());
                                addMessage(resultMessage);
                                frame.revalidate();
                                frame.repaint();
                            });
                        }

                        // チップ情報更新
                        if (message.startsWith("Your total chips:")) {
                            String chipStr = message.substring("Your total chips: ".length());
                            try {
                                int newChip = Integer.parseInt(chipStr);
                                player.setChip(newChip);
                                updatePlayerInfo();
                            } catch (NumberFormatException e) {
                                addMessage("Error parsing chip amount: " + chipStr);
                            }
                        }

                        // サーバーからの継続確認メッセージを受け取る
                        if (message.equals("CONTINUE?")) {
                            SwingUtilities.invokeLater(() -> {
                                // 既存のパネルをすべて削除
                                if (gamePanel.getActionPanel() != null
                                        && gamePanel.getActionPanel().getParent() != null) {
                                    gamePanel.getActionPanel().getParent().remove(gamePanel.getActionPanel());
                                }
                                if (gamePanel.getResultPanel() != null
                                        && gamePanel.getResultPanel().getParent() != null) {
                                    gamePanel.getResultPanel().getParent().remove(gamePanel.getResultPanel());
                                }

                                createContinuePanel();
                                frame.revalidate();
                                frame.repaint();
                            });
                        }
                        if (message.equals("SPECTATOR_NEXT_ROUND")) {
                            player.resetForNewRound();
                            addMessage("The previous game has ended. You can join the next round.");
                            readyButton.setEnabled(true);
                        }

                        if (message.equals("WAIT")) {
                            gamePanel.createMessagePanel("Waiting for other players...");
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

    private void sendReady() {
        if (isConnected) {
            out.println("OK");
            readyButton.setEnabled(false);
        }
    }

    private void disconnect() {
        if (isConnected) {
            out.println("END");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            isConnected = false;
            readyButton.setEnabled(false);
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

    // イベント処理
    public void onHit() {
        out.println("HIT");
        gamePanel.getHitButton().setEnabled(false);
        gamePanel.getStandButton().setEnabled(false);
    }

    public void onStand() {
        out.println("STAND");
        gamePanel.getHitButton().setEnabled(false);
        gamePanel.getStandButton().setEnabled(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new BJclient().show();
        });
    }

    // カードを追加表示するメソッド（HIT時に使用）
    private void addCardToPlayer(String cardCode) {
        SwingUtilities.invokeLater(() -> {
            player.setCard(cardCode);
            gamePanel.addPlayerCard(cardCode);
            // プレイヤーのスコアを更新
            int playerScore = player.getScore();
            gamePanel.updatePlayerScore(playerScore);
            if (playerScore > 21) {
                addMessage("You Busted!");
                gamePanel.createMessagePanel("You Busted!");
            }
            frame.revalidate();
            frame.repaint();
        });
    }

    // ディーラーのカードを追加表示するメソッド
    private void addCardToDealer(String cardCode) {
        SwingUtilities.invokeLater(() -> {
            dealerCardList.add(cardCode);
            gamePanel.addDealerCard(cardCode);
            // ディーラーのスコアを更新
            int dealerScore = calculateDealerScore();
            gamePanel.updateDealerScore(dealerScore);
            frame.revalidate();
            frame.repaint();
        });
    }

    // ディーラーのスコアを計算するメソッド
    private int calculateDealerScore() {
        int score = 0;
        int aceCount = 0;

        for (String card : dealerCardList) {
            String cardNum = card.substring(1);
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

    // 定期的に他のプレイヤー情報を更新するタイマーを開始
    private void startOtherPlayersUpdateTimer() {
        Timer timer = new Timer(2000, e -> {
            if (isConnected) {
                out.println("GET_OTHER_PLAYERS");
            }
        });
        timer.start();
    }
}
