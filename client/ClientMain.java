package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

// import server.ShellUI;

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

                        if (command.startsWith("upload ")) {
                            // Wait for READY from server
                            String ready = reader.readLine();
                            if ("READY".equals(ready)) {
                                // Send file size and file data
                                String filename = command.split(" ", 2)[1];
                                File file = new File(filename);
                                writer.write(file.length() + "\n");
                                writer.flush();

                                FileInputStream fis = new FileInputStream(file);
                                OutputStream os = socket.getOutputStream();
                                byte[] buffer = new byte[4096];
                                int count;
                                while ((count = fis.read(buffer)) > 0) {
                                    os.write(buffer, 0, count);
                                }
                                os.flush();
                                fis.close();

                                // Read server response until END_OF_RESPONSE
                                String uploadResponse;
                                while (!(uploadResponse = reader.readLine()).equals("END_OF_RESPONSE")) {
                                    System.out.println(uploadResponse);
                                }
                            } else {
                                System.out.println("Server did not respond with READY. Got: " + ready);
                            }
                        }

                        else if (command.startsWith("download ")) {
                            writer.write(command + "\n");
                            writer.flush();

                            // Read file size or error message from server
                            String fileSizeStr = reader.readLine();
                            long fileSize;
                            try {
                                fileSize = Long.parseLong(fileSizeStr);
                            } catch (NumberFormatException e) {
                                // Server sent an error message instead of file size
                                System.out.println(fileSizeStr);
                                // Read until END_OF_RESPONSE
                                String downloadResponse;
                                while (!(downloadResponse = reader.readLine()).equals("END_OF_RESPONSE")) {
                                    System.out.println(downloadResponse);
                                }
                                continue;
                            }

                            String filename = command.split(" ", 2)[1];
                            File outFile = new File(filename);
                            FileOutputStream fos = new FileOutputStream(outFile);
                            InputStream is = socket.getInputStream();
                            byte[] buffer = new byte[4096];
                            long remaining = fileSize;
                            int count;
                            while (remaining > 0
                                    && (count = is.read(buffer, 0, (int) Math.min(buffer.length, remaining))) > 0) {
                                fos.write(buffer, 0, count);
                                remaining -= count;
                            }
                            fos.close();

                            System.out.println("Download complete.");

                            // Read server response until END_OF_RESPONSE
                            String downloadResponse;
                            while (!(downloadResponse = reader.readLine()).equals("END_OF_RESPONSE")) {
                                System.out.println(downloadResponse);
                            }
                        } else {
                            // Read response(s) from server until marker (e.g., "END_OF_RESPONSE")
                            String response;
                            while (!(response = reader.readLine()).equals("END_OF_RESPONSE")) {
                                System.out.println(response);
                            }
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
