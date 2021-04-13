package cz.cvut.fit.psi.robots;

import java.util.ArrayList;
import java.util.List;

import static cz.cvut.fit.psi.robots.State.*;

public class StateMachine {
    private static final String ending = "\u0007\u0008";
    public static final String SERVER_MOVE = "102 MOVE" + ending;
    public static final String SERVER_TURN_LEFT = "103 TURN LEFT" + ending;
    public static final String SERVER_TURN_RIGHT = "104 TURN RIGHT" + ending;
    public static final String SERVER_PICK_UP = "105 GET MESSAGE" + ending;
    public static final String SERVER_LOGOUT = "106 LOGOUT" + ending;
    public static final String SERVER_KEY_REQUEST = "107 KEY REQUEST" + ending;
    public static final String SERVER_OK = "200 OK" + ending;
    public static final String SERVER_LOGIN_FAILED = "300 LOGIN FAILED" + ending;
    public static final String SERVER_SYNTAX_ERROR = "301 SYNTAX ERROR" + ending;
    public static final String SERVER_LOGIC_ERROR = "302 LOGIC ERROR" + ending;
    public static final String SERVER_KEY_OUT_OF_RANGE_ERROR = "303 KEY OUT OF RANGE" + ending;
    public static final String CLIENT_RECHARGING = "RECHARGING" + ending;
    public static final String CLIENT_FULL_POWER = "FULL POWER" + ending;

    private static final int TIMEOUT = 1000;            //timeout time in milliseconds
    private static final int TIMEOUT_CHARGING = 5000;   //timeout charging time in milliseconds

    private static List<KeyPair> keys;
    private Boolean charging;
    private State state;
    private Robot robot;
    private Integer ID;
    private Integer nameHash;
    private Integer clientHash;
    private String buffer = "";

    //todo create a class for responses?

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
        buffer = buffer.concat(input);
        int len = buffer.length();
        if (checkLength(len)) {
            return null;
        }
        return SERVER_SYNTAX_ERROR;
    }

    public String next(String input) {
        input = buffer.concat(input);
        buffer = "";
        //todo delete
//        System.out.println("input is: " + input);

        if (input.equals(CLIENT_RECHARGING)) {
            charging = true;
            return null;
        }
        if (!checkLength(input.length())) {         //msg is too long
            return SERVER_SYNTAX_ERROR;
        }
        if (charging) {                             //charging logic
            return input.equals(CLIENT_FULL_POWER) ? null : SERVER_LOGIC_ERROR;
        }
        if (input.equals(CLIENT_FULL_POWER)) {      //full power w/o charging
            return SERVER_SYNTAX_ERROR;
        }
        //todo this
//        input = input.substring(0, input.length() - 2);

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
                //todo will this trigger on its own or not?
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
            case ARRIVED -> {
                return SERVER_LOGOUT;
            }
        }
        return null; //todo?
    }

    private String initCheck(String input) {

        //todo delete
//        System.out.println("initCheck start");


        robot = new Robot(input);
        nameHash = robot.hash();
        state = S_KEY_SENT;


        //todo delete
//        System.out.println("initCheck end");
        return SERVER_KEY_REQUEST;
    }

    private String sKeyCheck(String input) {
        try {
            ID = Integer.parseInt(input);
            if (ID < 0 || ID > 4) {
                return SERVER_KEY_OUT_OF_RANGE_ERROR;
            } else {
                state = SERVER_CONFIRM;
                return getSHash().toString();
            }
        } catch (NumberFormatException e) {
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
        if (!robot.parse(input))
            return SERVER_SYNTAX_ERROR;
        state = SECOND_POS;
        return SERVER_MOVE;
    }

    private String secondPos(String input) {
        if (!robot.parse(input))
            return SERVER_SYNTAX_ERROR;
        if (robot.findDirection()) {
            state = NAVIGATING;
            return moveMsg(robot.nextMove());
        }
        return SERVER_TURN_RIGHT;
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
        return (clientHash - keys.get(ID).client) % 65536 == nameHash;
    }

    private boolean checkLength(int len) {
        if (charging)
            return len <= 12;

        switch (state) {
            //todo w/ or w/o \a\b??
            case START -> {
                return len <= 20;
            }
            case S_KEY_SENT -> {
                return len <= 5;
            }
            case SERVER_CONFIRM -> {
                return len <= 7;
            }
            case SERVER_OK_STATE,
                    NAVIGATING,
                    SECOND_POS,
                    FIRST_POS -> {
                return len <= 12;
            }
            case ARRIVED -> {
                return len <= 100;
            }
        }
        return true; //todo true or false
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
