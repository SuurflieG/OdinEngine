package com.odin.odinengine.world;

public class RaycastHit {

    private final int blockX;
    private final int blockY;
    private final int blockZ;
    private final short blockId;

    private final int previousX;
    private final int previousY;
    private final int previousZ;

    public RaycastHit(int blockX, int blockY, int blockZ, short blockId,
                      int previousX, int previousY, int previousZ) {
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
        this.blockId = blockId;
        this.previousX = previousX;
        this.previousY = previousY;
        this.previousZ = previousZ;
    }

    public int getBlockX() {
        return blockX;
    }

    public int getBlockY() {
        return blockY;
    }

    public int getBlockZ() {
        return blockZ;
    }

    public short getBlockId() {
        return blockId;
    }

    public int getPreviousX() {
        return previousX;
    }

    public int getPreviousY() {
        return previousY;
    }

    public int getPreviousZ() {
        return previousZ;
    }
}