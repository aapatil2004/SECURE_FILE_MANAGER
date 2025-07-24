package server;

import java.io.*;
import java.net.Socket;

public class ShellUI {

    private File currentDir;
    private BufferedWriter writer;

    public ShellUI(File currentDir, BufferedWriter writer) {
        this.currentDir = currentDir;
        this.writer = writer;
    }

    public void handleCommand(String input) throws IOException {
        String[] tokens = input.trim().split("\\s+");
        if (tokens.length == 0 || tokens[0].isEmpty()) {
            writer.write("No command entered\n");
            writer.flush();
            return;
        }
        String command = tokens[0];

        switch (command) {
            case "ls":
                File[] files = currentDir.listFiles();
                if (files == null || files.length == 0) {
                    writer.write("No files found.\n");
                } else {
                    for (File f : files) {
                        writer.write(f.getName() + (f.isDirectory() ? "/" : "") + "\n");
                    }
                }
                writer.write("END_OF_RESPONSE\n");
                writer.flush();
                break;

            case "pwd":
                writer.write("Current working directory: " + currentDir.getAbsolutePath() + "\n");
                writer.write("END_OF_RESPONSE\n");
                writer.flush();
                break;

            case "mkdir":
                if (tokens.length < 2) {
                    writer.write("Usage: mkdir <foldername>\n");
                } else {
                    File newFolder = new File(currentDir, tokens[1]);
                    if (newFolder.mkdir()) {
                        writer.write("Folder created: " + newFolder.getName() + "\n");
                    } else {
                        writer.write("Failed to create folder.\n");
                    }
                }
                writer.write("END_OF_RESPONSE\n");
                writer.flush();
                break;

            case "exit":
                writer.write("GoodBye!\n");
                writer.write("END_OF_RESPONSE\n");
                writer.flush();
                break;

            default:
                writer.write("Unknown command\n");
                writer.write("END_OF_RESPONSE\n");
                writer.flush();
        }
    }

    public void uploadCommand(String command, Socket socket) {
        try {
            String[] parts = command.split(" ", 2);
            String filename = parts[1];
            writer.write("READY\n");
            writer.flush();

            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            long fileSize = Long.parseLong(br.readLine());

            File outFile = new File(currentDir, filename);
            FileOutputStream fos = new FileOutputStream(outFile);

            InputStream is = socket.getInputStream();
            byte[] buffer = new byte[4096];
            long remaining = fileSize;
            int count;
            while (remaining > 0 && (count = is.read(buffer, 0, (int) Math.min(buffer.length, remaining))) > 0) {
                fos.write(buffer, 0, count);
                remaining -= count;
            }
            fos.close();

            writer.write("Upload complete.\nEND_OF_RESPONSE\n");
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void downloadCommand(String command, Socket socket) {
        try {
            String[] parts = command.split(" ", 2);
            String filename = parts[1];
            File file = new File(currentDir, filename);

            if (!file.exists()) {
                writer.write("File not found\nEND_OF_RESPONSE\n");
                writer.flush();
                return;
            }

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

            writer.write("END_OF_RESPONSE\n");
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
