import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

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
    private boolean isConnected = false;

    public BJclient() {
        createGUI();
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
        JLabel nameLabel = new JLabel("Name: ");
        JLabel idLabel = new JLabel("ID: ");
        JLabel chipLabel = new JLabel("Chip: ");
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
        
        readyButton.setEnabled(false);
        listButton.setEnabled(false);
        
        buttonPanel.add(readyButton);
        buttonPanel.add(listButton);
        buttonPanel.add(disconnectButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);

        // イベントリスナー
        readyButton.addActionListener(e -> sendReady());
        listButton.addActionListener(e -> sendList());
        disconnectButton.addActionListener(e -> disconnect());

        // 名前入力ダイアログ
        showNameDialog();
    }

    private void showNameDialog() {
        String name = JOptionPane.showInputDialog(frame, "Enter your name:", "Player Name", JOptionPane.PLAIN_MESSAGE);
        if (name != null && !name.trim().isEmpty()) {
            playerName = name.trim();
            connectToServer();
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
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            
            // 名前を送信
            out.println(playerName);
            
            // プレイヤー情報を受信
            playerID = Integer.parseInt(in.readLine());
            chip = Integer.parseInt(in.readLine());
            
            addMessage("Connected successfully!");
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
                        SwingUtilities.invokeLater(() -> addMessage(message));
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
}
