package com.odin.odinengine.world;

public enum Direction {
    FRONT(0, 0, 1),
    BACK(0, 0, -1),
    LEFT(-1, 0, 0),
    RIGHT(1, 0, 0),
    TOP(0, 1, 0),
    BOTTOM(0, -1, 0);

    private final int dx;
    private final int dy;
    private final int dz;

    Direction(int dx, int dy, int dz) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    public int dx() {
        return dx;
    }

    public int dy() {
        return dy;
    }

    public int dz() {
        return dz;
    }
}