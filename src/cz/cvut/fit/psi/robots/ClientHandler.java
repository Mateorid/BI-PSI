package cz.cvut.fit.psi.robots;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private static final String ending = "\u0007\u0008";
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private final StateMachine stateMachine;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        this.stateMachine = new StateMachine();
    }

    public void run() {
        String buffer = "";
        String output;
        try {
            while (true) {
                socket.setSoTimeout(stateMachine.getTimeout());
//                try {
//                    while (true) {
                String tmp = String.valueOf((char) in.read());
                buffer = buffer.concat(tmp);
//                    }

//                } catch (IOException ignored) {
//                }
                output = stateMachine.preCheck(buffer);
                buffer = "";

                if (!output.equals("")) {
                    if (output.equals(StateMachine.SERVER_SYNTAX_ERROR) ||
                            output.equals(StateMachine.SERVER_LOGIN_FAILED) ||
                            output.equals(StateMachine.SERVER_LOGIC_ERROR) ||
                            output.equals(StateMachine.SERVER_KEY_OUT_OF_RANGE_ERROR)) {
                        System.out.println("Server error: " + output);
                        out.write(output + ending);
                        out.flush();
                        break;
                    }
                    out.write(output + ending);
                    out.flush();
                    if (output.equals(StateMachine.SERVER_OK)) {
                        output = stateMachine.next("");
                        out.write(output + ending);
                        out.flush();
                    } else if (output.equals(StateMachine.SERVER_LOGOUT)) {
                        break;
                    }
                }
            }
        } catch (
                Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            exit();
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