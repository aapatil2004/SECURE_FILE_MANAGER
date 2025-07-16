package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 9090;

        try (Socket socket = new Socket(host, port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             Scanner scanner = new Scanner(System.in)) {

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);

                // Check if server expects user input
                if (line.toLowerCase().contains("username")
                        || line.toLowerCase().contains("password")
                        || line.toLowerCase().contains("choose option")
                        || line.toLowerCase().contains("command")) {

                    System.out.print(">> ");
                    String input = scanner.nextLine();
                    writer.write(input + "\n");
                    writer.flush();
                }

                if (line.toLowerCase().contains("disconnecting")) {
                    System.out.println("[!] Server disconnected.");
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
