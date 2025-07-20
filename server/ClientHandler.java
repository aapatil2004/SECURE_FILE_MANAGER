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
            ShellUI shell = new ShellUI(currentDir, writer);
            String input;
            while ((input = reader.readLine()) != null) {
                
                if (input.trim().equals("exit")) {
                    shell.handleCommand("exit");
                    clientSocket.close();
                    break;
                }
                shell.handleCommand(input);

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
