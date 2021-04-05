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

    //Robot information
    //todo make a class for this and put in stateMachine
//    private Integer x;
//    private Integer y;
//    private Direction direction;
//    private Integer idHash;


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
            socket.setSoTimeout(stateMachine.getTimeout());     //todo

            while (true) {
                line = in.readLine();
                if (line.endsWith("" + 0x7 + 0x8)) {        //todo works?
                    //todo pass to state machine here
                } else {
                    out.writeChars(StateMachine.SERVER_SYNTAX_ERROR);
                    out.flush();
                    exit();
                    break;
                }
            }
            exit();
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

    private void exit() {
        System.out.println("----EXITING----");
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}