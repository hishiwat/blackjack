import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GamePanel {
    private JPanel gamePanel;
    private JPanel playerCardsPanel;
    private JPanel dealerCardsPanel;
    private JPanel otherPlayersPanel;
    private JPanel betPanel;
    private JPanel actionPanel;
    private JPanel continuePanel;
    
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
        
        // ディーラータイトル
        JLabel dealerTitle = new JLabel("Dealer's Cards", SwingConstants.CENTER);
        dealerTitle.setForeground(Color.WHITE);
        dealerTitle.setFont(new Font("Arial", Font.BOLD, 18));
        dealerArea.add(dealerTitle, BorderLayout.NORTH);
        
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
        
        // プレイヤータイトル
        JLabel playerTitle = new JLabel("Your Cards", SwingConstants.CENTER);
        playerTitle.setForeground(Color.WHITE);
        playerTitle.setFont(new Font("Arial", Font.BOLD, 18));
        playerArea.add(playerTitle, BorderLayout.NORTH);
        
        // プレイヤーのカードパネル
        playerCardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        playerCardsPanel.setBackground(new Color(34, 139, 34));
        playerCardsPanel.setPreferredSize(new Dimension(0, 150)); // 最小高さを設定
        playerCardsPanel.setMinimumSize(new Dimension(0, 150));   // 最小高さを保証
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
        otherPlayersArea.setPreferredSize(new Dimension(250, 0));
        
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
    public JTextField getBetField() { return betField; }
    public JButton getBetButton() { return betButton; }
    public JButton getHitButton() { return hitButton; }
    public JButton getStandButton() { return standButton; }
    public JButton getContinueYesButton() { return continueYesButton; }
    public JButton getContinueNoButton() { return continueNoButton; }
} 