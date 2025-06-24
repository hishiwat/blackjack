import java.util.Scanner;
import java.io.*;
import java.net.*;

public class BJclient {
    public static void main(String[] args) {
        try (Scanner scan = new Scanner(System.in)) {
            int port = BJserver.PORT; // サーバと同じポート
            InetAddress addr = InetAddress.getByName("localhost");
            System.out.println("Connecting to server at " + addr + ":" + port);
            
            try (Socket socket = new Socket(addr, port)) {
                System.out.println("Connected: " + socket);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(
                        new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                System.out.print("Enter your name: ");
                String name = scan.nextLine();
                out.println(name);

                int ID = Integer.parseInt(in.readLine());
                int chip = Integer.parseInt(in.readLine());

                System.out.println("Your ID: " + ID);
                System.out.println("Your chip: " + chip);

                System.out.println("Type LIST to see current players, END to exit.");

                String str;
                while (true) {
                    str = scan.nextLine();
                    out.println(str);
                    if (str.equals("END")) break;

                    // プレイヤーリストの応答表示
                    if (str.equals("LIST")) {
                        String line;
                        while (!(line = in.readLine()).equals("=== End of List ===")) {
                            System.out.println(line);
                        }
                        System.out.println(line);
                    }
                }

                System.out.println("Disconnected.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
