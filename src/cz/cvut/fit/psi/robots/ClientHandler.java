package cz.cvut.fit.psi.robots;

import java.io.*;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.Scanner;

// ClientHandler class inspired from: https://www.geeksforgeeks.org/multithreaded-servers-in-java/
public class ClientHandler implements Runnable {
    private static final String ending = "\u0007\u0008";
    Socket socket;
    //    BufferedReader in;
    private final Scanner in;
    PrintWriter out;
    StateMachine stateMachine;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
//        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream())); //todo does this work?
        this.in = new Scanner(new BufferedReader(new InputStreamReader(socket.getInputStream()))).useDelimiter(ending); //todo does this work?
        this.out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        this.stateMachine = new StateMachine();
    }

    public void run() {
//        String buffer = "";
//        String output = "";
//        long xdd = 0;
//        char tmp;
        String line = "";
        try {
            while (true) {
                socket.setSoTimeout(stateMachine.getTimeout());     //todo - here?

//                if (in.ready()) {
//                while (in.ready()) {
//                        buffer = buffer.concat(String.valueOf((char) in.read()));
//                        System.out.println(buffer);
//                        if (test2) {
//                            test = true;
//                            test2 = false;
//                        }
//                }
//                if (buffer.endsWith(ending)) {
//                    output = stateMachine.next(buffer);
//                    buffer="";
//                } else {
//                    output = stateMachine.preCheck(buffer);
//                    buffer="";
//                }

                if (in.hasNext()) {
                    line = stateMachine.next(in.next());
//                    System.out.println("writing chars: " + line);
                    if (!line.equals(""))
                        out.write(line);
                    else
                        continue;
                    if (line.equals(StateMachine.SERVER_SYNTAX_ERROR) ||
                            line.equals(StateMachine.SERVER_LOGIN_FAILED) ||
                            line.equals(StateMachine.SERVER_LOGIC_ERROR) ||
                            line.equals(StateMachine.SERVER_KEY_OUT_OF_RANGE_ERROR)) {
                        out.flush();
//                        System.out.println("flushed");
                        break;
                    }
                    out.flush();
//                    System.out.println("flushed");
                }

//                else {
//                    try {
//                        line = in.next();
//                    } catch (Exception e) {
////                        e.printStackTrace();
//                        line = "";
//                        continue;
//                    }
//                    out.writeChars(stateMachine.preCheck(line));
//                    line = stateMachine.preCheck(in.next());
//                    if (line.equals(StateMachine.SERVER_LOGIN_FAILED) ||
//                            line.equals(StateMachine.SERVER_SYNTAX_ERROR) ||
//                            line.equals(StateMachine.SERVER_LOGIC_ERROR) ||
//                            line.equals(StateMachine.SERVER_KEY_OUT_OF_RANGE_ERROR)) {
//                        out.flush();
//                        break;
//                    }
//                    out.flush();
//                }


//                line = in.readLine();
//                if (line.endsWith("" + 0x7 + 0x8)) {        //todo works?
//                    todo check if sent message is SERVER_OK & if it is send empty msg to get the server to ask for COORDINATES
//                    out.writeChars(stateMachine.next(line));
//                    out.flush();
//                } else {//todo this is wrong since it can send only parts of the message :(
//                    out.writeChars(StateMachine.SERVER_SYNTAX_ERROR);
//                    out.flush();
//                    exit();
//                    break;
//                }
//            } catch (InterruptedException | IOException interruptedException) {
//            interruptedException.printStackTrace();
            }
//        exit();
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