import javax.swing.*;
import java.awt.*;

public class CardDisplay {
    
    /**
     * カードコードからカードラベルを作成
     */
    public static JLabel createCardLabel(String cardCode) {
        // カードの内容を解析
        String suit = cardCode.substring(0, 1);
        String rank = cardCode.substring(1);
        
        // スートの色を決定
        Color suitColor = (suit.equals("H") || suit.equals("D")) ? Color.RED : Color.BLACK;
        
        // スート記号
        String suitSymbol = getSuitSymbol(suit);
        
        return new JLabel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                
                // 縦横比を2:3（標準的なトランプカードの比率）に調整
                double aspectRatio = 2.0 / 3.0; // 幅:高さ = 2:3
                int cardWidth = width;
                int cardHeight = height;
                
                // 親コンポーネントのサイズに基づいてカードサイズを調整
                if (getParent() != null) {
                    Dimension parentSize = getParent().getSize();
                    if (parentSize.width > 0 && parentSize.height > 0) {
                        // 親の高さに基づいてカードの高さを決定
                        int maxCardHeight = (int) (parentSize.height * 0.7); // 親の高さの70%
                        cardHeight = Math.max(80, Math.min(maxCardHeight, 120)); // 最小80px、最大120px
                        cardWidth = (int) (cardHeight * aspectRatio);
                        
                        // カードの位置を中央に調整
                        int x = (width - cardWidth) / 2;
                        int y = (height - cardHeight) / 2;
                        
                        // カードの描画領域を調整
                        width = cardWidth;
                        height = cardHeight;
                        
                        // 描画位置を調整
                        g2d.translate(x, y);
                    }
                }
                
                // カードの影
                g2d.setColor(new Color(0, 0, 0, 50));
                g2d.fillRoundRect(3, 3, width-6, height-6, 8, 8);
                
                // カードの背景（グラデーション効果）
                GradientPaint gradient = new GradientPaint(0, 0, Color.WHITE, width, height, new Color(245, 245, 245));
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, width-3, height-3, 8, 8);
                
                // カードの枠線
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(0, 0, width-3, height-3, 8, 8);
                
                // 内側の枠線
                g2d.setColor(new Color(200, 200, 200));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(2, 2, width-7, height-7, 6, 6);
                
                // フォントサイズを動的に調整
                int smallFontSize = Math.max(12, height / 8);
                int largeFontSize = Math.max(20, height / 4);
                int margin = Math.max(4, width / 20);
                
                // 左上のランクとスート
                g2d.setColor(suitColor);
                g2d.setFont(new Font("Arial", Font.BOLD, smallFontSize));
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(rank, margin, margin + fm.getAscent());
                g2d.drawString(suitSymbol, margin, margin + fm.getAscent() * 2);
                
                // 中央のスート（大きく）
                g2d.setFont(new Font("Arial", Font.BOLD, largeFontSize));
                fm = g2d.getFontMetrics();
                int centerX = width / 2 - fm.stringWidth(suitSymbol) / 2;
                int centerY = height / 2 + fm.getAscent() / 2;
                g2d.drawString(suitSymbol, centerX, centerY);
                
                // 右下のランクとスート（回転）
                g2d.setFont(new Font("Arial", Font.BOLD, smallFontSize));
                fm = g2d.getFontMetrics();
                int bottomX = width - fm.stringWidth(rank) - margin;
                int bottomY = height - margin;
                g2d.drawString(rank, bottomX, bottomY);
                
                int bottomSuitX = width - fm.stringWidth(suitSymbol) - margin;
                int bottomSuitY = height - margin - fm.getAscent();
                g2d.drawString(suitSymbol, bottomSuitX, bottomSuitY);

            }
            
            @Override
            public Dimension getPreferredSize() {
                // 親コンポーネントのサイズに基づいてカードサイズを決定
                if (getParent() != null) {
                    Dimension parentSize = getParent().getSize();
                    if (parentSize.width > 0 && parentSize.height > 0) {
                        // 親の高さに基づいてカードの高さを決定
                        int maxCardHeight = (int) (parentSize.height * 0.7); // 親の高さの70%
                        int cardHeight = Math.max(80, Math.min(maxCardHeight, 120)); // 最小80px、最大120px
                        int cardWidth = (int) (cardHeight * 2.0 / 3.0); // 縦横比2:3
                        return new Dimension(cardWidth, cardHeight);
                    }
                }
                // デフォルトサイズ
                return new Dimension(80, 120);
            }
            
            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
            
            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };
    }
    
    /**
     * スート記号を取得
     */
    private static String getSuitSymbol(String suit) {
        switch (suit) {
            case "H": return "♥";
            case "D": return "♦";
            case "C": return "♣";
            case "S": return "♠";
            default: return "?";
        }
    }
}