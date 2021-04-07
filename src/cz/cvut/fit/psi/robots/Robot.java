package cz.cvut.fit.psi.robots;

import java.util.Objects;

public class Robot {
    private Integer x;
    private Integer y;
    //    private Integer idHash;
    private final String name;
    private Direction direction;

    public Robot(String name) {
        this.name = name;
    }

    public Boolean atFinish() {
        return (x == 0 && y == 0);
    }

    //todo stuff here

    public Integer getX() {
        return x;
    }

    public Integer getY() {
        return y;
    }

    public String getName() {
        return name;
    }

    public int hash() {
        int tmp = 0;
        for (int i = 0; i < name.length(); i++) {
            tmp += name.charAt(i);
        }
        return ((tmp * 1000) % 65536);
    }

    public Direction getDirection() {
        return direction;
    }
}
