package server;

import java.io.*;

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
}
