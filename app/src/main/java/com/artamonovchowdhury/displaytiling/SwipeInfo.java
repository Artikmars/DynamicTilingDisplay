package com.artamonovchowdhury.displaytiling;


public class SwipeInfo {
    public Direction dir;

    public float angle;

    public SwipeInfo(Direction dir, float angle) {
        this.dir = dir;
        this.angle = angle;
    }

    public Direction getDir() {
        return dir;
    }

    public float getAngle() {
        return angle;
    }

    public String getDirectionAsString() {
        if (dir == Direction.LEFT) {
            return "LEFT";
        }
        if (dir == Direction.RIGHT) {
            return "RIGHT";
        }
        if (dir == Direction.UP) {
            return "UP";
        }
        if (dir == Direction.DOWN) {
            return "DOWN";
        }
        return "Error";
    }
}
