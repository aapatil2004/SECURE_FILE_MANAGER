package server;

import java.net.ServerSocket;
import java.net.Socket;


public class ServerMain
{
    public static void main(String[] args)
    {
        int port = 9090;
        System.out.println("[+] Starting Secure Remote File Transfer server ");

        try(ServerSocket serversocket = new ServerSocket(port))
        {
            System.out.println("Server listening to port " +port);

            while(true)
            {
                Socket clientSocket = serversocket.accept();
                System.out.println("[+] New client connected from " + clientSocket.getInetAddress());

                //Handle client in a seprate thread ( to be added later)
                new Thread(new ClientHandler(clientSocket)).start();
            }
        }

        catch( Exception e)
        {
            e.printStackTrace();
        }
    }
}

