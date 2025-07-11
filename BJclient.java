import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    
    // 新しいGUI要素
    private GamePanel gamePanel;
    private boolean cardsProcessing = false; // カード処理中フラグ
    private JLabel playerInfoLabel; // プレイヤー情報表示用ラベル

    // GUI更新用にラベルをフィールドとして宣言
    private JLabel nameLabel;
    private JLabel idLabel;
    private JLabel chipLabel;

    public BJclient() {
		showNameDialog();
        createGUI();
        connectToServer();
    }

    private void createGUI() {
        frame = new JFrame("Blackjack Casino Table");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);
        
        // カジノテーブル風の背景色
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
    
    private void createTopInfoPanel(JPanel mainPanel) {
        Color casinoGreen = new Color(34, 139, 34);
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(casinoGreen);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // プレイヤー情報
        playerInfoLabel = new JLabel("Name: " + (playerName != null ? playerName : "") + " | ID: " + playerID + " | Chips: " + chip);
        playerInfoLabel.setForeground(Color.WHITE);
        playerInfoLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topPanel.add(playerInfoLabel, BorderLayout.WEST);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
    }
    
    // createGameAreaメソッドは削除（GamePanelクラスで処理）
    
    private void createControlPanel(JPanel mainPanel) {
        Color casinoGreen = new Color(34, 139, 34);
        JPanel controlPanel = new JPanel(new BorderLayout(10, 10));
        controlPanel.setBackground(casinoGreen);
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // メッセージエリア
        messageArea = new JTextArea(5, 40);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setBackground(new Color(245, 245, 245));
        messageArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Game Messages"));
        controlPanel.add(scrollPane, BorderLayout.CENTER);
        
        // ボタンパネル
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setBackground(casinoGreen);
        
        readyButton = new JButton("Ready");
        listButton = new JButton("Show Players");
        JButton disconnectButton = new JButton("Disconnect");
        
        // ボタンのスタイル設定
        Color buttonColor = new Color(70, 130, 180);
        readyButton.setBackground(buttonColor);
        readyButton.setForeground(Color.WHITE);
        listButton.setBackground(buttonColor);
        listButton.setForeground(Color.WHITE);
        disconnectButton.setBackground(new Color(220, 20, 60));
        disconnectButton.setForeground(Color.WHITE);
        
        readyButton.setEnabled(false);
        listButton.setEnabled(false);
        
        buttonPanel.add(readyButton);
        buttonPanel.add(listButton);
        buttonPanel.add(disconnectButton);
        
        controlPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        
        // イベントリスナー
        readyButton.addActionListener(e -> sendReady());
        listButton.addActionListener(e -> sendList());
        disconnectButton.addActionListener(e -> disconnect());
    }
    
    private void createBetPanel() {
        gamePanel.createBetPanel();
        gamePanel.getBetButton().addActionListener(e -> placeBet());
        frame.revalidate();
        frame.repaint();
    }
    
    private void createActionPanel() {
        gamePanel.createActionPanel();
        gamePanel.getHitButton().addActionListener(e -> sendHit());
        gamePanel.getStandButton().addActionListener(e -> sendStand());
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
    
    private JLabel createCardLabel(String cardCode) {
        return CardDisplay.createCardLabel(cardCode);
    }
    
    private void updatePlayerInfo() {
        if (playerInfoLabel != null) {
            playerInfoLabel.setText("Name: " + (playerName != null ? playerName : "") + " | ID: " + playerID + " | Chips: " + chip);
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
            addMessage("[DEBUG] Sent bet: " + bet);
            player.chipBet(bet);
            chip = player.getChip(); // チップ情報を更新
			updatePlayerInfo();
            
            // ベットパネルを非表示
            gamePanel.removePanel(gamePanel.getBetPanel());
            frame.revalidate();
            frame.repaint();
            addMessage("[DEBUG] Bet panel removed, waiting for cards...");
            
        } catch (NumberFormatException ex) {
            addMessage("Please enter a valid number.");
        }
    }
    
    private void sendHit() {
        out.println("HIT");
        addMessage("Sent: HIT");
        gamePanel.getHitButton().setEnabled(false);
        gamePanel.getStandButton().setEnabled(false);
    }
    
    private void sendStand() {
        out.println("STAND");
        addMessage("Sent: STAND");
        gamePanel.getHitButton().setEnabled(false);
        gamePanel.getStandButton().setEnabled(false);
    }
    
    private void sendContinueYes() {
        out.println("CONTINUE_YES");
        addMessage("Sent: CONTINUE_YES (継続します)");
        readyButton.setEnabled(true);
        gamePanel.removePanel(gamePanel.getContinuePanel());
        frame.revalidate();
        frame.repaint();
    }
    
    private void sendContinueNo() {
        out.println("CONTINUE_NO");
        addMessage("Sent: CONTINUE_NO (ゲームを終了します)");
        disconnect();
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
                    SwingUtilities.invokeLater(() -> updatePlayerInfo());
                    // ゲーム途中の参加の場合は観戦モード
                    if (response.equals("Inprogress")) {
                        addMessage("You are in spectator mode");
                        player.setState(PlayerState.SPECTATOR);
                    }

                    break;
                }
            }

            isConnected = true;
            readyButton.setEnabled(true);
            listButton.setEnabled(true);

            // サーバーからのメッセージ受信用スレッド
            Thread readerThread = new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        final String message = line;
                        System.out.println("Received: " + message); // デバッグ用 受信したメッセージを標準出力に表示
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
                            SwingUtilities.invokeLater(() -> {
                                createBetPanel();
                                addMessage("Game started! Please place your bet.");
                            });
                        }
                        if (message.equals("Game Reset")) {
                            player.resetForNewRound();
                            readyButton.setEnabled(true);
                        }

                        if (message.equals("Cards") && !cardsProcessing) {
                            addMessage("[DEBUG] Received Cards message, starting card collection...");
                            cardsProcessing = true; // フラグを設定
                                try {
                                // 2枚のカードを受け取りリストに格納
                                ArrayList<String> playerCards = new ArrayList<>();
                                
                                for (int i = 0; i < 2; i++) {
                                    String card = in.readLine();
                                    addMessage("[DEBUG] Received player card " + (i+1) + ": " + card);
                                        playerCards.add(card);
                                }
                                
                                // ディーラーのカード
                                    String dealerLine = in.readLine();
                                    addMessage("[DEBUG] Dealer line: " + dealerLine);
                                    String dealerCard = dealerLine.substring("Dealer Card ".length()).trim();
                                
                                addMessage("[DEBUG] Collected " + playerCards.size() + " player cards: " + playerCards);
                                addMessage("[DEBUG] Dealer card: " + dealerCard);
                                
                                // すべてのカードを収集した後、UIを更新
                                final ArrayList<String> finalPlayerCards = playerCards;
                                final String finalDealerCard = dealerCard;
                                
                                SwingUtilities.invokeLater(() -> {
                                                                            // プレイヤーのカードをクリアして表示
                                        gamePanel.clearPlayerCards();
                                        addMessage("[DEBUG] Cleared player cards panel");
                                        
                                        // プレイヤーのカードを表示
                                        for (String card : finalPlayerCards) {
                                            player.setCard(card);
                                            addMessage("[DEBUG] Creating card label for: " + card);
                                            gamePanel.addPlayerCard(card);
                                            addMessage("Your card: " + card);
                                        }
                                        
                                        addMessage("[DEBUG] Player cards panel component count: " + gamePanel.getPlayerCardsPanel().getComponentCount());
                                        addMessage("[DEBUG] Player cards panel size: " + gamePanel.getPlayerCardsPanel().getSize());
                                        
                                        // ディーラーのカードを表示
                                        if (finalDealerCard != null) {
                                            dealerCardList.add(finalDealerCard);
                                            gamePanel.clearDealerCards();
                                            gamePanel.addDealerCard(finalDealerCard);
                                            addMessage("Dealer's Card: " + finalDealerCard);
                                        }
                                    
                                                                            // HIT/STANDボタンを表示
                                        createActionPanel();
                                        addMessage("Your turn! Choose HIT or STAND.");
                                        frame.revalidate();
                                        frame.repaint();
                                        addMessage("[DEBUG] Cards processing completed");
                                        cardsProcessing = false; // フラグをリセット
                                });
                                
                            } catch (IOException e) {
                                addMessage("[ERROR] Failed to process cards: " + e.getMessage());
                                e.printStackTrace();
                            } finally {
                                cardsProcessing = false; // フラグをリセット
                            }
                        }

                        // プレイヤーリスト情報
                        if (message.startsWith("PLAYER_LIST:")) {
                            String playerListInfo = message.substring("PLAYER_LIST:".length());
                            updateOtherPlayers(playerListInfo);
                        }
                        
                        // HIT後のカード追加
                        if (message.startsWith("HIT_CARD:")) {
                            String cardCode = message.substring("HIT_CARD:".length());
                            addCardToPlayer(cardCode);
                        }
                        
                        // ディーラーのカード追加
                        if (message.startsWith("DEALER_CARD:")) {
                            String cardCode = message.substring("DEALER_CARD:".length());
                            addCardToDealer(cardCode);
                        }
                        
                        // ゲーム結果
                        if (message.startsWith("RESULT:")) {
                            String result = message.substring("RESULT:".length());
                            SwingUtilities.invokeLater(() -> {
                                addMessage("Game Result: " + result);
                                // アクションパネルを非表示
                                if (gamePanel.getActionPanel() != null) {
                                    gamePanel.removePanel(gamePanel.getActionPanel());
                                    frame.revalidate();
                                    frame.repaint();
                                }
                            });
                        }
                        
                        // 継続確認メッセージ
                        if (message.equals("CONTINUE?")) {
                            SwingUtilities.invokeLater(() -> {
                                createContinuePanel();
                                addMessage("Do you want to continue playing?");
                            });
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new BJclient().show();
        });
    }

    // 他のプレイヤーの情報を表示するメソッド
    private void updateOtherPlayers(String playerListInfo) {
        SwingUtilities.invokeLater(() -> {
            gamePanel.updateOtherPlayers(playerListInfo, playerName);
            frame.revalidate();
            frame.repaint();
        });
    }
    
    // カードを追加表示するメソッド（HIT時に使用）
    private void addCardToPlayer(String cardCode) {
        SwingUtilities.invokeLater(() -> {
            player.setCard(cardCode);
            gamePanel.addPlayerCard(cardCode);
            addMessage("Your new card: " + cardCode);
            frame.revalidate();
            frame.repaint();
        });
    }
    
    // ディーラーのカードを追加表示するメソッド
    private void addCardToDealer(String cardCode) {
        SwingUtilities.invokeLater(() -> {
            dealerCardList.add(cardCode);
            gamePanel.addDealerCard(cardCode);
            addMessage("Dealer's new card: " + cardCode);
            frame.revalidate();
            frame.repaint();
        });
    }
}
