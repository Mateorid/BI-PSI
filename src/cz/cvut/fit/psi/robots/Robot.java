package cz.cvut.fit.psi.robots;

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

    public Direction getDirection() {
        return direction;
    }
}
