import javax.swing.*;
import java.awt.*;

public class GamePanel {
    private JPanel gamePanel;
    private JPanel playerCardsPanel;
    private JPanel dealerCardsPanel;
    private JPanel otherPlayersPanel;
    private JLabel playerScoreLabel;
    private JLabel dealerScoreLabel;
    private JPanel betPanel;
    private JPanel actionPanel;
    private JPanel continuePanel;
    private JPanel resultPanel;
    
    // ベット関連
    private JTextField betField;
    private JButton betButton;
    
    // アクション関連
    private JButton hitButton;
    private JButton standButton;
    
    // 継続関連
    private JButton continueYesButton;
    private JButton continueNoButton;
    
    public GamePanel() {
        createGamePanel();
    }
    
    private void createGamePanel() {
        gamePanel = new JPanel(new BorderLayout(10, 10));
        gamePanel.setBackground(new Color(34, 139, 34)); // カジノテーブル風の緑色
        gamePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // ディーラーエリア（上部）
        createDealerArea();
        
        // プレイヤーエリア（下部）
        createPlayerArea();
        
        // 他のプレイヤーエリア（右側）
        createOtherPlayersArea();
    }
    
    private void createDealerArea() {
        JPanel dealerArea = new JPanel(new BorderLayout());
        dealerArea.setBackground(new Color(34, 139, 34));
        dealerArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // ディーラータイトルとスコア
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(34, 139, 34));
        
        JLabel dealerTitle = new JLabel("Dealer's Cards", SwingConstants.CENTER);
        dealerTitle.setForeground(Color.WHITE);
        dealerTitle.setFont(new Font("Arial", Font.BOLD, 18));
        
        dealerScoreLabel = new JLabel("Score: -", SwingConstants.RIGHT);
        dealerScoreLabel.setForeground(Color.YELLOW);
        dealerScoreLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        titlePanel.add(dealerTitle, BorderLayout.CENTER);
        titlePanel.add(dealerScoreLabel, BorderLayout.EAST);
        
        dealerArea.add(titlePanel, BorderLayout.NORTH);
        
        // ディーラーのカードパネル
        dealerCardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        dealerCardsPanel.setBackground(new Color(34, 139, 34));
        dealerCardsPanel.setPreferredSize(new Dimension(0, 100)); // 最小高さを設定
        dealerCardsPanel.setMinimumSize(new Dimension(0, 100));   // 最小高さを保証
        dealerArea.add(dealerCardsPanel, BorderLayout.CENTER);
        
        gamePanel.add(dealerArea, BorderLayout.NORTH);
    }
    
    private void createPlayerArea() {
        JPanel playerArea = new JPanel(new BorderLayout());
        playerArea.setBackground(new Color(34, 139, 34));
        playerArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // プレイヤータイトルとスコア
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(34, 139, 34));
        
        JLabel playerTitle = new JLabel("Your Cards", SwingConstants.CENTER);
        playerTitle.setForeground(Color.WHITE);
        playerTitle.setFont(new Font("Arial", Font.BOLD, 18));
        
        playerScoreLabel = new JLabel("Score: -", SwingConstants.RIGHT);
        playerScoreLabel.setForeground(Color.YELLOW);
        playerScoreLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        titlePanel.add(playerTitle, BorderLayout.CENTER);
        titlePanel.add(playerScoreLabel, BorderLayout.EAST);
        
        playerArea.add(titlePanel, BorderLayout.NORTH);
        
        // プレイヤーのカードパネル
        playerCardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        playerCardsPanel.setBackground(new Color(34, 139, 34));
        playerCardsPanel.setPreferredSize(new Dimension(500, 150)); // 最小高さを設定
        playerCardsPanel.setMinimumSize(new Dimension(500, 150));   // 最小高さを保証
        playerArea.add(playerCardsPanel, BorderLayout.CENTER);
        
        gamePanel.add(playerArea, BorderLayout.CENTER);
    }
    
    private void createOtherPlayersArea() {
        JPanel otherPlayersArea = new JPanel(new BorderLayout());
        otherPlayersArea.setBackground(new Color(34, 139, 34));
        otherPlayersArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        otherPlayersArea.setPreferredSize(new Dimension(500, 150));
        
        // 他のプレイヤータイトル
        JLabel otherPlayersTitle = new JLabel("Other Players", SwingConstants.CENTER);
        otherPlayersTitle.setForeground(Color.WHITE);
        otherPlayersTitle.setFont(new Font("Arial", Font.BOLD, 16));
        otherPlayersArea.add(otherPlayersTitle, BorderLayout.NORTH);
        
        // 他のプレイヤーパネル
        otherPlayersPanel = new JPanel();
        otherPlayersPanel.setLayout(new BoxLayout(otherPlayersPanel, BoxLayout.Y_AXIS));
        otherPlayersPanel.setBackground(new Color(34, 139, 34));
        
        JScrollPane scrollPane = new JScrollPane(otherPlayersPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        otherPlayersArea.add(scrollPane, BorderLayout.CENTER);
        
        gamePanel.add(otherPlayersArea, BorderLayout.EAST);
    }
    
    public void createBetPanel() {
        // 既存のベットパネルを削除
        if (betPanel != null && betPanel.getParent() != null) {
            betPanel.getParent().remove(betPanel);
        }
        
        // 既存のアクションパネルを削除
        if (actionPanel != null && actionPanel.getParent() != null) {
            actionPanel.getParent().remove(actionPanel);
        }
        
        // 既存の結果パネルを削除
        if (resultPanel != null && resultPanel.getParent() != null) {
            resultPanel.getParent().remove(resultPanel);
        }
        
        // 既存の継続パネルを削除
        if (continuePanel != null && continuePanel.getParent() != null) {
            continuePanel.getParent().remove(continuePanel);
        }
        
        betPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        betPanel.setBackground(new Color(34, 139, 34));
        betPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel betLabel = new JLabel("Place your bet:");
        betLabel.setForeground(Color.WHITE);
        betLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        betField = new JTextField(10);
        betField.setFont(new Font("Arial", Font.PLAIN, 14));
        
        betButton = new JButton("Bet");
        betButton.setBackground(new Color(70, 130, 180));
        betButton.setForeground(Color.WHITE);
        betButton.setFont(new Font("Arial", Font.BOLD, 14));
        
        betPanel.add(betLabel);
        betPanel.add(betField);
        betPanel.add(betButton);
        
        gamePanel.add(betPanel, BorderLayout.SOUTH);
    }

    public void createGameActionPanel() {
        // 既存のアクションパネルを削除
        if (actionPanel != null && actionPanel.getParent() != null) {
            actionPanel.getParent().remove(actionPanel);
        }
        
        // 既存のベットパネルを削除
        if (betPanel != null && betPanel.getParent() != null) {
            betPanel.getParent().remove(betPanel);
        }
        
        // 既存の結果パネルを削除
        if (resultPanel != null && resultPanel.getParent() != null) {
            resultPanel.getParent().remove(resultPanel);
        }
        
        // 既存の継続パネルを削除
        if (continuePanel != null && continuePanel.getParent() != null) {
            continuePanel.getParent().remove(continuePanel);
        }
        
        actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        actionPanel.setBackground(new Color(34, 139, 34));
        actionPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        hitButton = new JButton("HIT");
        hitButton.setBackground(new Color(50, 205, 50));
        hitButton.setForeground(Color.WHITE);
        hitButton.setFont(new Font("Arial", Font.BOLD, 16));
        hitButton.setPreferredSize(new Dimension(100, 40));
        
        standButton = new JButton("STAND");
        standButton.setBackground(new Color(255, 140, 0));
        standButton.setForeground(Color.WHITE);
        standButton.setFont(new Font("Arial", Font.BOLD, 16));
        standButton.setPreferredSize(new Dimension(100, 40));
        
        actionPanel.add(hitButton);
        actionPanel.add(standButton);
        
        gamePanel.add(actionPanel, BorderLayout.SOUTH);
    }
    
    public void createActionPanel() {
        actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        actionPanel.setBackground(new Color(34, 139, 34));
        actionPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        hitButton = new JButton("HIT");
        hitButton.setBackground(new Color(50, 205, 50));
        hitButton.setForeground(Color.WHITE);
        hitButton.setFont(new Font("Arial", Font.BOLD, 16));
        hitButton.setPreferredSize(new Dimension(100, 40));
        
        standButton = new JButton("STAND");
        standButton.setBackground(new Color(220, 20, 60));
        standButton.setForeground(Color.WHITE);
        standButton.setFont(new Font("Arial", Font.BOLD, 16));
        standButton.setPreferredSize(new Dimension(100, 40));
        
        actionPanel.add(hitButton);
        actionPanel.add(standButton);
        
        gamePanel.add(actionPanel, BorderLayout.SOUTH);
    }
    
    public void createContinuePanel() {
        // 既存の継続パネルを削除
        if (continuePanel != null && continuePanel.getParent() != null) {
            continuePanel.getParent().remove(continuePanel);
        }
        
        // 既存の結果パネルを削除
        if (resultPanel != null && resultPanel.getParent() != null) {
            resultPanel.getParent().remove(resultPanel);
        }
        
        continuePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        continuePanel.setBackground(new Color(34, 139, 34));
        continuePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel continueLabel = new JLabel("Continue playing?");
        continueLabel.setForeground(Color.WHITE);
        continueLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        continueYesButton = new JButton("YES");
        continueYesButton.setBackground(new Color(50, 205, 50));
        continueYesButton.setForeground(Color.WHITE);
        continueYesButton.setFont(new Font("Arial", Font.BOLD, 14));
        continueYesButton.setPreferredSize(new Dimension(80, 35));
        
        continueNoButton = new JButton("NO");
        continueNoButton.setBackground(new Color(220, 20, 60));
        continueNoButton.setForeground(Color.WHITE);
        continueNoButton.setFont(new Font("Arial", Font.BOLD, 14));
        continueNoButton.setPreferredSize(new Dimension(80, 35));
        
        continuePanel.add(continueLabel);
        continuePanel.add(continueYesButton);
        continuePanel.add(continueNoButton);
        
        gamePanel.add(continuePanel, BorderLayout.SOUTH);
    }

	public void createResultPanel(String result, int chipChange) {
        // 既存の結果パネルを削除
        if (resultPanel != null && resultPanel.getParent() != null) {
            resultPanel.getParent().remove(resultPanel);
        }
        
        // 既存のアクションパネルを削除
        if (actionPanel != null && actionPanel.getParent() != null) {
            actionPanel.getParent().remove(actionPanel);
        }

        resultPanel = new JPanel(new BorderLayout(10, 10));
        resultPanel.setBackground(new Color(34, 139, 34));
        resultPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 215, 0), 3),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // 結果メッセージ
        JLabel resultLabel = new JLabel(result, SwingConstants.CENTER);
        resultLabel.setForeground(Color.WHITE);
        resultLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        // チップ変動
        JLabel chipLabel = new JLabel("Chip change: " + (chipChange >= 0 ? "+" : "") + chipChange, SwingConstants.CENTER);
        chipLabel.setForeground(chipChange >= 0 ? new Color(50, 205, 50) : new Color(255, 99, 71));
        chipLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        centerPanel.setBackground(new Color(34, 139, 34));
        centerPanel.add(resultLabel);
        centerPanel.add(chipLabel);
        
        resultPanel.add(centerPanel, BorderLayout.CENTER);
        
        gamePanel.add(resultPanel, BorderLayout.SOUTH);
    }
    
    public void addPlayerCard(String cardCode) {
        JLabel cardLabel = CardDisplay.createCardLabel(cardCode);
        playerCardsPanel.add(cardLabel);
        playerCardsPanel.revalidate();
        playerCardsPanel.repaint();
    }
    
    public void addDealerCard(String cardCode) {
        JLabel cardLabel = CardDisplay.createCardLabel(cardCode);
        dealerCardsPanel.add(cardLabel);
        dealerCardsPanel.revalidate();
        dealerCardsPanel.repaint();
    }

    public void updatePlayerScore(int score) {
        if (playerScoreLabel != null && score != -1) {
            playerScoreLabel.setText("Score: " + score);
        } else {
            playerScoreLabel.setText("Score: -");
        }
    }

    public void updateDealerScore(int score) {
        if (dealerScoreLabel != null && score != -1) {
            dealerScoreLabel.setText("Score: " + score);
        } else {
            dealerScoreLabel.setText("Score: -");
        }
    }
    
    public void clearPlayerCards() {
        playerCardsPanel.removeAll();
        playerCardsPanel.revalidate();
        playerCardsPanel.repaint();
    }
    
    public void clearDealerCards() {
        dealerCardsPanel.removeAll();
        dealerCardsPanel.revalidate();
        dealerCardsPanel.repaint();
    }
    
    public void updateOtherPlayers(String playerListInfo, String currentPlayerName) {
        otherPlayersPanel.removeAll();
        
        if (playerListInfo != null && !playerListInfo.trim().isEmpty()) {
            String[] players = playerListInfo.split(",");
            for (String player : players) {
                if (!player.trim().isEmpty() && !player.contains(currentPlayerName)) {
                    JLabel playerLabel = new JLabel(player.trim());
                    playerLabel.setForeground(Color.WHITE);
                    playerLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                    playerLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                    otherPlayersPanel.add(playerLabel);
                }
            }
        }
        
        otherPlayersPanel.revalidate();
        otherPlayersPanel.repaint();
    }

    public void updateOtherPlayersDetailed(String otherPlayersInfo, String currentPlayerName) {
        otherPlayersPanel.removeAll();
        
        if (otherPlayersInfo != null && !otherPlayersInfo.trim().isEmpty()) {
            String[] players = otherPlayersInfo.split(";");
            for (String playerInfo : players) {
                if (!playerInfo.trim().isEmpty()) {
                    String[] parts = playerInfo.split("\\|");
                    if (parts.length >= 4) {
                        String name = parts[0];
                        String chips = parts[1];
                        String bet = parts[2];
                        String state = parts[3];
                        
                        // プレイヤー情報パネルを作成
                        JPanel playerPanel = new JPanel(new BorderLayout());
                        playerPanel.setBackground(new Color(34, 139, 34));
                        playerPanel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(255, 215, 0), 1),
                            BorderFactory.createEmptyBorder(5, 5, 5, 5)
                        ));
                        
                        // プレイヤー名と基本情報
                        JPanel infoPanel = new JPanel(new GridLayout(4, 1, 2, 2));
                        infoPanel.setBackground(new Color(34, 139, 34));
                        
                        JLabel nameLabel = new JLabel("Name: " + name);
                        nameLabel.setForeground(Color.WHITE);
                        nameLabel.setFont(new Font("Arial", Font.BOLD, 12));
                        
                        JLabel chipLabel = new JLabel("Chips: " + chips);
                        chipLabel.setForeground(Color.WHITE);
                        chipLabel.setFont(new Font("Arial", Font.PLAIN, 11));
                        
                        JLabel betLabel = new JLabel("Bet: " + bet);
                        betLabel.setForeground(Color.WHITE);
                        betLabel.setFont(new Font("Arial", Font.PLAIN, 11));
                        
                        // スコア計算と表示
                        int score = 0;
                        if (parts.length > 4 && !parts[4].isEmpty()) {
                            String[] cards = parts[4].split(",");
                            score = calculateScore(cards);
                        }
                        JLabel scoreLabel = new JLabel("Score: " + score);
                        scoreLabel.setForeground(Color.YELLOW);
                        scoreLabel.setFont(new Font("Arial", Font.BOLD, 11));
                        
                        infoPanel.add(nameLabel);
                        infoPanel.add(chipLabel);
                        infoPanel.add(betLabel);
                        infoPanel.add(scoreLabel);
                        
                        playerPanel.add(infoPanel, BorderLayout.NORTH);
                        
                        // カード情報（ゲーム中のみ）
                        if (parts.length > 4 && !parts[4].isEmpty()) {
                            JPanel cardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
                            cardsPanel.setBackground(new Color(34, 139, 34));
                            
                            String[] cards = parts[4].split(",");
                            for (String card : cards) {
                                if (!card.trim().isEmpty()) {
                                    JLabel cardLabel = CardDisplay.createCardLabel(card.trim());
                                    cardsPanel.add(cardLabel);
                                }
                            }
                            
                            playerPanel.add(cardsPanel, BorderLayout.CENTER);
                        }
                        
                        // 状態表示
                        JLabel stateLabel = new JLabel("State: " + state);
                        stateLabel.setForeground(Color.YELLOW);
                        stateLabel.setFont(new Font("Arial", Font.ITALIC, 10));
                        playerPanel.add(stateLabel, BorderLayout.SOUTH);
                        
                        otherPlayersPanel.add(playerPanel);
                        otherPlayersPanel.add(Box.createVerticalStrut(5)); // 間隔を追加
                    }
                }
            }
        }
        
        otherPlayersPanel.revalidate();
        otherPlayersPanel.repaint();
    }

    // スコア計算メソッド
    private int calculateScore(String[] cards) {
        int score = 0;
        int aceCount = 0;
        
        for (String card : cards) {
            if (!card.trim().isEmpty()) {
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
        }
        
        while (score > 21 && aceCount > 0) {
            score -= 10;
            aceCount--;
        }
        
        return score;
    }
    
    public void removePanel(JPanel panel) {
        if (panel != null && panel.getParent() != null) {
            panel.getParent().remove(panel);
        }
    }
    
    // Getter methods
    public JPanel getGamePanel() { return gamePanel; }
    public JPanel getPlayerCardsPanel() { return playerCardsPanel; }
    public JPanel getDealerCardsPanel() { return dealerCardsPanel; }
    public JPanel getOtherPlayersPanel() { return otherPlayersPanel; }
    public JPanel getBetPanel() { return betPanel; }
    public JPanel getActionPanel() { return actionPanel; }
    public JPanel getContinuePanel() { return continuePanel; }
    public JPanel getResultPanel() { return resultPanel; }
    public JTextField getBetField() { return betField; }
    public JButton getBetButton() { return betButton; }
    public JButton getHitButton() { return hitButton; }
    public JButton getStandButton() { return standButton; }
    public JButton getContinueYesButton() { return continueYesButton; }
    public JButton getContinueNoButton() { return continueNoButton; }
} 