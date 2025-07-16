package server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private BufferedReader reader;
    private BufferedWriter writer;

    private File currentDir;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            writer.write("Welcome to Secure Remote File Manager!\n");
            writer.write("1. Login\n2. SignUp\nChoose option: \n");
            writer.flush();

            String choice = reader.readLine();

            writer.write("Enter username:\n");
            writer.flush();
            String username = reader.readLine();

            writer.write("Enter password:\n");
            writer.flush();
            String password = reader.readLine();

            if (choice.equals("2")) {
                if (AuthService.signup(username, password)) {
                    writer.write("SignUp successful. You are now logged in!\n");
                    writer.flush();

                    String userDirPath = "storage/" + username;
                    File userDir = new File(userDirPath);
                    if (!userDir.exists()) {
                        userDir.mkdirs();
                    }
                    this.currentDir = userDir;
                } else {
                    writer.write("Signup failed. Username may already exist...\n");
                    writer.flush();
                    clientSocket.close();
                    return;
                }
            } else {
                if (AuthService.login(username, password)) {
                    writer.write("Login Successful\n");
                    writer.flush();
                    String userDirPath = "storage/" + username;
                    File userDir = new File(userDirPath);
                    if (!userDir.exists()) {
                        userDir.mkdirs();
                    }
                    this.currentDir = userDir;
                } else {
                    writer.write("Login Failed ...\n");
                    writer.flush();
                    clientSocket.close();
                    return;
                }
            }

            writer.write("command :\n");
            writer.flush();
            String input;
            while ((input = reader.readLine()) != null) {
                String[] tokens = input.trim().split("\\s+");
                String command = tokens[0];

                switch (command) {
                    case "ls":
                        if (currentDir == null) {
                            writer.write("Directory not initialized.\n");
                        } else {
                            File[] files = currentDir.listFiles();
                            if (files == null || files.length == 0) {
                                writer.write("No files found.\n");
                            } else {
                                for (File f : files) {
                                    writer.write(f.getName() + (f.isDirectory() ? "/\n" : "\n"));
                                }
                            }
                        }
                        writer.flush();
                        break;
                    case "pwd":
                        writer.write("Current working directory: " + currentDir.getAbsolutePath() + "\n");
                        writer.flush();
                        break;
                    case "mkdir":
                        if (tokens.length < 2)
                            writer.write("Usage: mkdir <foldername>\n");
                        else {
                            File newFolder = new File(currentDir, tokens[1]);
                            if (newFolder.mkdir()) {
                                writer.write("Folder created: " + newFolder.getName() + "\n");
                            } else {
                                writer.write("Failed to create folder. \n");
                            }
                        }
                        writer.flush();
                        break;
                    case "exit":
                        writer.write("GoodBye !\n");
                        writer.flush();
                        clientSocket.close();
                        return;
                    default:
                        writer.write("Unknown command\n");
                        writer.flush();
                }
            }
        }

        catch (IOException e) {
            System.out.println("[-] Client Disconnected. ");
        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
