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

                // Loop for interactive shell
                if (line.equalsIgnoreCase("Login Successful")) {
                    while (true) {
                        System.out.print("\ncommand:\n>> ");
                        String command = scanner.nextLine();
                        writer.write(command + "\n");
                        writer.flush();

                        // Read response(s) from server until marker (e.g., "END_OF_RESPONSE")
                        String response;
                        while (!(response = reader.readLine()).equals("END_OF_RESPONSE")) {
                            System.out.println(response);
                        }

                        // Optionally exit shell if user types "logout" or "exit"
                        if (command.equalsIgnoreCase("logout") || command.equalsIgnoreCase("exit")) {
                            break;
                        }
                    }
                    break; // Break main while-loop after logout
                }

                // Normal input prompts (before login)
                if (line.toLowerCase().contains("username")
                        || line.toLowerCase().contains("password")
                        || line.toLowerCase().contains("choose option")
                        || line.endsWith(":")) {
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
