package cz.cvut.fit.psi.robots;

import java.io.*;
import java.net.*;


//Server class from: https://www.geeksforgeeks.org/multithreaded-servers-in-java/
class Server {
    public static void main(String[] args) {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(6969);
            socket.setReuseAddress(true);

            while (true) {
                Socket client = socket.accept();
                System.out.println("New client connected: " + client.getInetAddress().getHostAddress());

                ClientHandler clientSock = new ClientHandler(client);
                new Thread(clientSock).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
