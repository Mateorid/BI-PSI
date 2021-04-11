package cz.cvut.fit.psi.robots;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cz.cvut.fit.psi.robots.Direction.*;

public class Robot {
    private Integer x = 0;
    private Integer y = 0;
    private int prevX;
    private int prevY;
    private boolean first = true;
    private final String name;
    private Direction direction;
    private static final Pattern p = Pattern.compile("OK (-?\\d+) (-?\\d+)");

    public Robot(String name) {
        this.name = name;
    }

    public boolean atFinish() {
        return (x == 0 && y == 0);
    }

    //inspired from: https://devqa.io/extract-numbers-string-java-regular-expressions/
    public boolean parse(String input) {
        if (input.length() > 12) { //todo 12 or less (10)?
            return false;
        }
        Matcher m = p.matcher(input);
        if (m.matches()) {
            x = Integer.parseInt(m.group(1));
            y = Integer.parseInt(m.group(2));
            if (first) { //todo save this every time?
                prevX = x;
                prevY = y;
                first = false;
            }
            return true;
        }
        return false;
    }

    public boolean findDirection() {
        if (prevX > x) {
            direction = LEFT;
            return true;
        }
        if (prevX < x) {
            direction = RIGHT;
            return true;
        }
        if (prevY > y) {
            direction = DOWN;
            return true;
        }
        if (prevY < y) {
            direction = UP;
            return true;
        }
        return false; //we've hit an obstacle -> need to rotate
    }

    public int nextMove() { // -1 -> LEFT | 1 -> RIGHT | 0 -> MOVE | 2 -> at destination
        if (x < 0) { //need to go right
            switch (direction) {
                case UP, LEFT -> {
                    return 1;
                }
                case DOWN -> {
                    return -1;
                }
                case RIGHT -> {
                    return 0;
                }
            }
        }
        if (x > 0) { //need to go left
            switch (direction) {
                case UP, RIGHT -> {
                    return -1;
                }
                case DOWN -> {
                    return 1;
                }
                case LEFT -> {
                    return 0;
                }
            }
        }
        if (y < 0) { //need to go up
            switch (direction) {
                case UP -> {
                    return 0;
                }
                case DOWN, LEFT -> {
                    return 1;
                }
                case RIGHT -> {
                    return -1;
                }
            }
        }
        if (y > 0) { //need to go down
            switch (direction) {
                case UP, LEFT -> {
                    return -1;
                }
                case DOWN -> {
                    return 0;
                }
                case RIGHT -> {
                    return 1;
                }
            }
        }
        return 2; //we're at 0:0 -> pick up the msg
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
