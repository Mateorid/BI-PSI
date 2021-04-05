package cz.cvut.fit.psi.robots;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

// ClientHandler class inspired from: https://www.geeksforgeeks.org/multithreaded-servers-in-java/
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final BufferedReader in;
    //    private final Scanner in;
    private final DataOutputStream out;
    private final StateMachine stateMachine;

    private Integer x;
    private Integer y;


    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream())); //todo does this work?
//        this.in = new Scanner(new BufferedReader(new InputStreamReader(socket.getInputStream()))).useDelimiter("\\a\\b"); //todo does this work?
        this.out = new DataOutputStream(socket.getOutputStream());
        this.stateMachine = new StateMachine();
    }

    public void run() {
        String line;

        try {
            socket.setSoTimeout(socket.getSoTimeout()); //todo

            while (true) {
                line = in.readLine();
                if (line.endsWith("" + 0x7 + 0x8)) {
                    //todo
                }else{
                    //todo
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}