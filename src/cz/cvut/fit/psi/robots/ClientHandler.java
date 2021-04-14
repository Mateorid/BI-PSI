package cz.cvut.fit.psi.robots;

import java.io.*;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.Scanner;

// ClientHandler class inspired from: https://www.geeksforgeeks.org/multithreaded-servers-in-java/
public class ClientHandler implements Runnable {
    private static final String ending = "\u0007\u0008";
    Socket socket;
    BufferedReader in;
    //    private final Scanner in;
//    OutputStreamWriter out;
    PrintWriter out;
    StateMachine stateMachine;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream())); //todo does this work?
//        this.in = new Scanner(new BufferedReader(new InputStreamReader(socket.getInputStream()))).useDelimiter(ending); //todo does this work?
        this.out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
//        this.out = new OutputStreamWriter(socket.getOutputStream());
//        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.stateMachine = new StateMachine();
    }

    public void run() {
        String buffer = "";
        String output = "";
        String line = "";
        try {
            //todo server doesn't time out for normal msg? (14) / implement obstacle dodging (20 or 25) :)

            while (true) {
                socket.setSoTimeout(stateMachine.getTimeout());

                if (in.ready()) {
                    buffer = buffer.concat(String.valueOf((char) in.read()));
//                    System.out.println("buffer is: " + buffer);
                }
                if (buffer.endsWith(ending)) {
                    output = stateMachine.next(buffer.substring(0, buffer.length() - 2));
                    buffer = "";
                }
//                } else {
//                    output = stateMachine.preCheck(buffer);
//                    buffer = "";
//                }
                if (output != "") {
                    if (output.equals(StateMachine.SERVER_SYNTAX_ERROR) ||
                            output.equals(StateMachine.SERVER_LOGIN_FAILED) ||
                            output.equals(StateMachine.SERVER_LOGIC_ERROR) ||
                            output.equals(StateMachine.SERVER_KEY_OUT_OF_RANGE_ERROR)) {
                        System.out.println("Server error: " + output);
                        out.write(output + ending);
                        out.flush();
                        exit();
                        break;
                    }
                    System.out.println("Output is: " + output);
                    out.write(output + ending);
                    out.flush();
                    if (output.equals(StateMachine.SERVER_OK)) {
                        output = stateMachine.next("");
                        out.write(output + ending);
                        out.flush();
                    } else if (output.equals(StateMachine.SERVER_LOGOUT)) {
                        exit();
                        break;
                    }
                    output = "";
                }

            }
        } catch (Exception e) {
            System.out.println("Exception: " + e);
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