package cz.cvut.fit.psi.robots;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cz.cvut.fit.psi.robots.Direction.*;

public class Robot {
    private int x, y, prevX, prevY, steps = 0;
    private boolean first = true;
    private boolean moved = false;
    private boolean stuck = false;
    private final String name;
    private Direction direction;
    private static final Pattern p = Pattern.compile("OK (-?\\d+) (-?\\d+)");

    public Robot(String name) {
        this.name = name;
    }

    //inspired from: https://devqa.io/extract-numbers-string-java-regular-expressions/
    public boolean parse(String input) {
        Matcher m = p.matcher(input);
        if (m.matches()) {
            if (!first) {
                prevX = x;
                prevY = y;
            }
            x = Integer.parseInt(m.group(1));
            y = Integer.parseInt(m.group(2));
            if (first) {
                prevX = x;
                prevY = y;
                first = false;
            }
            return true;
        }
        return false;
    }

    public int findDirection() {
        if (prevX > x) {
            direction = LEFT;
            return 1;
        }
        if (prevX < x) {
            direction = RIGHT;
            return 1;
        }
        if (prevY > y) {
            direction = DOWN;
            return 1;
        }
        if (prevY < y) {
            direction = UP;
            return 1;
        }
        if (stuck) {
            stuck = false;
            return -1;
        }
        stuck = true;
        return 0; //we've hit an obstacle -> need to rotate
    }

    public int nextMove() { // -1 -> LEFT | 1 -> RIGHT | 0 -> MOVE | 2 -> at destination
        if (stuck || (moved && prevX == x && prevY == y)) {
            stuck = true;
            moved = false;
            return obstacleHandler();
        }

        if (x < 0) { //need to go right
            switch (direction) {
                case UP -> {
                    direction = RIGHT;
                    moved = false;
                    return 1;
                }
                case DOWN -> {
                    direction = RIGHT;
                    moved = false;
                    return -1;
                }
                case LEFT -> {
                    direction = UP;
                    moved = false;
                    return 1;
                }
                case RIGHT -> {
                    moved = true;
                    return 0;
                }
            }
        }
        if (x > 0) { //need to go left
            switch (direction) {
                case UP -> {
                    direction = LEFT;
                    moved = false;
                    return -1;
                }
                case DOWN -> {
                    direction = LEFT;
                    moved = false;
                    return 1;
                }
                case LEFT -> {
                    moved = true;
                    return 0;
                }
                case RIGHT -> {
                    direction = UP;
                    moved = false;
                    return -1;
                }
            }
        }
        if (y < 0) { //need to go up
            switch (direction) {
                case UP -> {
                    moved = true;
                    return 0;
                }
                case DOWN -> {
                    direction = LEFT;
                    moved = false;
                    return 1;
                }
                case LEFT -> {
                    direction = UP;
                    moved = false;
                    return 1;
                }
                case RIGHT -> {
                    direction = UP;
                    moved = false;
                    return -1;
                }
            }
        }
        if (y > 0) { //need to go down
            switch (direction) {
                case UP -> {
                    direction = LEFT;
                    moved = false;
                    return -1;
                }
                case DOWN -> {
                    moved = true;
                    return 0;
                }
                case LEFT -> {
                    direction = DOWN;
                    moved = false;
                    return -1;
                }
                case RIGHT -> {
                    direction = DOWN;
                    moved = false;
                    return 1;
                }
            }
        }
        return 2; //we're at 0:0 -> pick up the msg
    }

    // -1 -> LEFT | 1 -> RIGHT | 0 -> MOVE | 2 -> at destination
    private Integer obstacleHandler() {
        //Should really make this better xd
        switch (steps) {
            case 0 -> {
                steps++;
                return 1;
            }
            case 1, 3 -> {
                steps++;
                return 0;
            }
            case 2 -> {
                steps++;
                return -1;
            }
            case 4 -> {
                steps = 0;
                stuck = false;
                return 0;
            }
        }
        return 2; //this shouldn't happen
    }

    public int hash() {
        int tmp = 0;
        for (int i = 0; i < name.length(); i++) {
            tmp += name.charAt(i);
        }
        return ((tmp * 1000) % 65536);
    }
}
