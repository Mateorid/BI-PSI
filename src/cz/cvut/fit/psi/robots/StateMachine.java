package cz.cvut.fit.psi.robots;

public class StateMachine {
    private static final String SERVER_PASSWORD = "101 PASSWORD" + 0x7 + 0x8;
    private static final String SERVER_MOVE = "102 MOVE" + 0x7 + 0x8;
    private static final String SERVER_TURN_LEFT = "103 TURN LEFT" + 0x7 + 0x8;
    private static final String SERVER_TURN_RIGHT = "104 TURN RIGHT" + 0x7 + 0x8;
    private static final String SERVER_PICK_UP = "105 GET MESSAGE" + 0x7 + 0x8;
    private static final String SERVER_LOGOUT = "106 LOGOUT" + 0x7 + 0x8;
    private static final String SERVER_KEY_REQUEST = "107 KEY REQUEST" + 0x7 + 0x8;
    private static final String SERVER_OK = "200 OK" + 0x7 + 0x8;
    private static final String SERVER_LOGIN_FAILED = "300 LOGIN FAILED" + 0x7 + 0x8;
    private static final String SERVER_SYNTAX_ERROR = "301 SYNTAX ERROR" + 0x7 + 0x8;
    private static final String SERVER_LOGIC_ERROR = "302 LOGIC ERROR" + 0x7 + 0x8;
    private static final String SERVER_KEY_OUT_OF_RANGE_ERROR = "303 KEY OUT OF RANGE" + 0x7 + 0x8;

    private static final Integer TIMEOUT = 1;
    private static final Integer TIMEOUT_RECHARGING = 5;

    private Boolean charging;
    private State state;
    private Direction direction;

    public StateMachine() {
        this.charging = false;
    }

    int getTimeout() {
        return charging ? TIMEOUT_RECHARGING : TIMEOUT;
    }


}
