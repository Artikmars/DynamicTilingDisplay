package com.artamonovchowdhury.displaytiling;

import static java.lang.Math.atan;
import static java.lang.Math.toDegrees;

public class Vector2d {
    private final float x, y;

    public Vector2d(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float x() {
        return this.x;
    }

    public float y() {
        return this.y;
    }

    public float angle(Vector2d v, Direction dir) {
        float a, b;
        if (dir == Direction.RIGHT || dir == Direction.LEFT) {
            a = abs(this.x() - v.x());
            b = abs(this.y() - v.y());
            return (float) toDegrees(atan((double) b / a));
        } else if (dir == Direction.UP || dir == Direction.DOWN) {
            a = abs(this.y() - v.y());
            b = abs(this.x() - v.x());
            return (float) toDegrees(atan((double) b / a));
        }
        return 0.0f;
    }

    private float abs(float v) {
        if (v < 0) {
            return v * (-1);
        } else {
            return v;
        }
    }

}
