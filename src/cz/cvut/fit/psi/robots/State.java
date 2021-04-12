package cz.cvut.fit.psi.robots;

public enum State {
    START,              // waiting for username
    S_KEY_SENT,         // waiting for client ID
    SERVER_CONFIRM,     // waiting for client key-hash
    SERVER_OK_STATE,    // hashes are ok
    FIRST_POS,          // first move
    SECOND_POS,         // 2nd move
    NAVIGATING,         // getting to [0,0]
    //    CHARGING,           // robot is charging
    ARRIVED;            // at [0,0]
}
