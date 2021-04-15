package cz.cvut.fit.psi.robots;

import java.util.ArrayList;
import java.util.List;

import static cz.cvut.fit.psi.robots.State.*;

public class StateMachine {
    private static final String ending = "\u0007\u0008";
    public static final String SERVER_MOVE = "102 MOVE";
    public static final String SERVER_TURN_LEFT = "103 TURN LEFT";
    public static final String SERVER_TURN_RIGHT = "104 TURN RIGHT";
    public static final String SERVER_PICK_UP = "105 GET MESSAGE";
    public static final String SERVER_LOGOUT = "106 LOGOUT";
    public static final String SERVER_KEY_REQUEST = "107 KEY REQUEST";
    public static final String SERVER_OK = "200 OK";
    public static final String SERVER_LOGIN_FAILED = "300 LOGIN FAILED";
    public static final String SERVER_SYNTAX_ERROR = "301 SYNTAX ERROR";
    public static final String SERVER_LOGIC_ERROR = "302 LOGIC ERROR";
    public static final String SERVER_KEY_OUT_OF_RANGE_ERROR = "303 KEY OUT OF RANGE";
    public static final String CLIENT_RECHARGING = "RECHARGING";
    public static final String CLIENT_FULL_POWER = "FULL POWER";

    private static final int TIMEOUT = 1000;            //timeout time in milliseconds
    private static final int TIMEOUT_CHARGING = 5000;   //timeout charging time in milliseconds

    private static List<KeyPair> keys;
    private boolean charging;
    private State state;
    private Robot robot;
    private Integer ID;
    private Integer nameHash;
    private Integer clientHash;
    private String buffer = "";

    public StateMachine() {
        state = State.START;
        charging = false;
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

    public String preCheck(String input) {
        int index;
        int len;
        String out;

        buffer = buffer.concat(input);
        index = buffer.indexOf(ending);
        if (index == -1) {
            len = buffer.length();
            if (!checkLength(len) && !buffer.startsWith("REC")) {
                return SERVER_SYNTAX_ERROR;
            }
            return "";
        } else {
            out = buffer.substring(0, index);
            len = out.length();
            if (!checkLength(len) && !buffer.startsWith("REC")) {
                return SERVER_SYNTAX_ERROR;
            }
            buffer = buffer.substring(index + 2);
            return next(out);
        }
    }

    public String next(String input) {

        if (input.equals(CLIENT_RECHARGING)) {
            charging = true;
            return "";
        }
        if (!checkLength(input.length())) {         //msg is too long
            return SERVER_SYNTAX_ERROR;
        }
        if (charging) {                             //charging logic
            charging = false;
            return input.equals(CLIENT_FULL_POWER) ? "" : SERVER_LOGIC_ERROR;
        }
        if (input.equals(CLIENT_FULL_POWER)) {      //full power w/o charging
            return SERVER_SYNTAX_ERROR;
        }

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
                return SERVER_TURN_RIGHT;
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
            case ARRIVED -> {
                return SERVER_LOGOUT;
            }
        }
        return "";
    }

    private String initCheck(String input) {
        robot = new Robot(input);
        nameHash = robot.hash();
        state = S_KEY_SENT;
        return SERVER_KEY_REQUEST;
    }

    private String sKeyCheck(String input) {
        try {
            ID = Integer.parseInt(input.trim());
            if (ID < 0 || ID > 4) {
                return SERVER_KEY_OUT_OF_RANGE_ERROR;
            } else {
                state = SERVER_CONFIRM;
                return getSHash().toString();
            }
        } catch (NumberFormatException e) {
            System.out.println("error: " + e);
            return SERVER_SYNTAX_ERROR;
        }
    }

    private String cKeyCheck(String input) {
        try {
            clientHash = Integer.parseInt(input);
            if (!checkCHash()) {
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
        if (!robot.parse(input))
            return SERVER_SYNTAX_ERROR;
        state = SECOND_POS;
        return SERVER_MOVE;
    }

    private String secondPos(String input) {
        if (!robot.parse(input))
            return SERVER_SYNTAX_ERROR;
        switch (robot.findDirection()) {
            case 1:
                state = NAVIGATING;
                return moveMsg(robot.nextMove());
            case 0:
                return SERVER_TURN_RIGHT;
            case -1:
                return SERVER_MOVE;
        }
        return SERVER_MOVE;
    }

    private String navigate(String input) {
        if (!robot.parse(input))
            return SERVER_SYNTAX_ERROR;
        return moveMsg(robot.nextMove());
    }

    private Integer getSHash() {
        return (nameHash + keys.get(ID).server) % 65536;
    }

    private boolean checkCHash() {
        return (nameHash + keys.get(ID).client) % 65536 == clientHash;
    }

    private boolean checkLength(int len) {
        if (charging)
            return len <= 19;

        switch (state) {
            case START -> {
                return len <= 19;
            }
            case S_KEY_SENT -> {
                return len <= 4;
            }
            case SERVER_CONFIRM -> {
                return len <= 6;
            }
            case SERVER_OK_STATE,
                    NAVIGATING,
                    SECOND_POS,
                    FIRST_POS -> {
                return len <= 11;
            }
            case ARRIVED -> {
                return len <= 99;
            }
        }
        return true;
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
        return SERVER_MOVE; //this shouldn't happen
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
