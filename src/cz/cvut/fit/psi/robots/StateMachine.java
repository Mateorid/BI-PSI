package cz.cvut.fit.psi.robots;

import java.util.ArrayList;
import java.util.List;

import static cz.cvut.fit.psi.robots.State.*;

public class StateMachine {
    public static final String SERVER_MOVE = "102 MOVE" + 0x7 + 0x8;
    public static final String SERVER_TURN_LEFT = "103 TURN LEFT" + 0x7 + 0x8;
    public static final String SERVER_TURN_RIGHT = "104 TURN RIGHT" + 0x7 + 0x8;
    public static final String SERVER_PICK_UP = "105 GET MESSAGE" + 0x7 + 0x8;
    public static final String SERVER_LOGOUT = "106 LOGOUT" + 0x7 + 0x8;
    public static final String SERVER_KEY_REQUEST = "107 KEY REQUEST" + 0x7 + 0x8;
    public static final String SERVER_OK = "200 OK" + 0x7 + 0x8;
    public static final String SERVER_LOGIN_FAILED = "300 LOGIN FAILED" + 0x7 + 0x8;
    public static final String SERVER_SYNTAX_ERROR = "301 SYNTAX ERROR" + 0x7 + 0x8;
    public static final String SERVER_LOGIC_ERROR = "302 LOGIC ERROR" + 0x7 + 0x8;
    public static final String SERVER_KEY_OUT_OF_RANGE_ERROR = "303 KEY OUT OF RANGE" + 0x7 + 0x8;

    private static final int TIMEOUT = 1000;            //timeout time in milliseconds
    private static final int TIMEOUT_CHARGING = 5000;   //timeout charging time in milliseconds
    private static final String ending = "\u0007\u0008";
    /*alternative:*/
//    char TERM_SEQ = (char)7;
//    char TERM_SEQ2 = (char)8;

    private static List<KeyPair> keys;
    private Boolean charging;
    private State state;
    private Robot robot;
    private Integer ID;
    private Integer nameHash;
    private Integer clientHash;
//    private String inputBuffer; //todo will have to buffer input for when we dont get the whole message?


    //todo create a class for responses?

    public StateMachine() {
        state = State.START;
        this.charging = false;
        keys = new ArrayList<>();
        keys.add(new KeyPair(23019, 32037));
        keys.add(new KeyPair(32037, 29295));
        keys.add(new KeyPair(18789, 13603));
        keys.add(new KeyPair(16443, 29533));
        keys.add(new KeyPair(18189, 21952));
    }

    public int getTimeout() {
        return charging ? TIMEOUT_CHARGING : TIMEOUT;
    }

    public String next(String input) {
        //todo this will be the main logic
        switch (state) {
            case START -> {
                return initCheck(input);
            }
            case S_KEY_SENT -> {
                return sKeyCheck(input);
            }
            case SERVER_CONFIRM -> {
                return cKeyCheck(input);
            }
            case SERVER_OK_STATE -> {
                state = FIRST_POS;
                return SERVER_TURN_LEFT;
            }
            case FIRST_POS -> {
                return firstPos(input);
            }
            case SECOND_POS -> {
                return secondPos(input);
            }
            case NAVIGATING -> {
                return navigate(input);
            }
            case CHARGING -> {
            }
            case ARRIVED -> {
            }
        }
        return SERVER_SYNTAX_ERROR; //todo?
    }

    private String initCheck(String input) {
        if (input.length() < 20) {
            //todo exit?
            return SERVER_SYNTAX_ERROR;
        }
        robot = new Robot(input.substring(0, input.length() - 2));
        nameHash = robot.hash();
        state = S_KEY_SENT;
        return SERVER_KEY_REQUEST;
    }

    private String sKeyCheck(String input) {
        try {
            ID = Integer.parseInt(input);
            if (ID < 0 || ID > 4) {
                //todo exit?
                return SERVER_KEY_OUT_OF_RANGE_ERROR;
            } else {
                state = SERVER_CONFIRM;
                return getSHash().toString();
            }
        } catch (NumberFormatException e) {
            //todo exit?
            return SERVER_SYNTAX_ERROR;
        }
    }

    private String cKeyCheck(String input) {
        try {
            clientHash = Integer.parseInt(input);
            if (!checkCHash()) {
                //todo exit?
                return SERVER_LOGIN_FAILED;
            } else {
                state = SERVER_OK_STATE;
                return SERVER_OK;
            }
        } catch (NumberFormatException e) {
            return SERVER_SYNTAX_ERROR;
        }
    }

    private String firstPos(String input) {
        robot.parse(input); //todo perform bool check (and then test for charging)
        state = SECOND_POS;
        return SERVER_MOVE;
    }

    private String secondPos(String input) {
        robot.parse(input);//todo perform bool check (and then test for charging)
        if (robot.findDirection()) {
            state = NAVIGATING;
            return moveMsg(robot.nextMove());
        }
        return SERVER_TURN_RIGHT;
    }

    private String navigate(String input) {
        robot.parse(input); //todo perform bool check (and then test for charging)
        return moveMsg(robot.nextMove());
    }

    private Integer getSHash() {
        return (nameHash + keys.get(ID).server) % 65536;
    }

    private boolean checkCHash() {
        return (clientHash - keys.get(ID).client) % 65536 == nameHash;
    }

    private String moveMsg(int i) {
        switch (i) {
            case -1 -> {
                return SERVER_TURN_LEFT;
            }
            case 0 -> {
                return SERVER_MOVE;
            }
            case 1 -> {
                return SERVER_TURN_RIGHT;
            }
            case 2 -> {
                state = ARRIVED;
                return SERVER_PICK_UP;
            }
        }
        return SERVER_MOVE; //this should happen
    }

    static class KeyPair {
        int server;
        int client;

        public KeyPair(int server, int client) {
            this.server = server;
            this.client = client;
        }
    }
}
