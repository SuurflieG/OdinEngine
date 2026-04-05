package com.odin.odinengine.world;

public class RaycastHit {
    private final int blockX;
    private final int blockY;
    private final int blockZ;
    private final short blockId;

    // The face of the hit block that the ray entered through.
    // Example: if the ray was moving in +X and entered a block,
    // the face hit is LEFT.
    private final Direction hitFace;

    public RaycastHit(int blockX, int blockY, int blockZ, short blockId, Direction hitFace) {
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
        this.blockId = blockId;
        this.hitFace = hitFace;
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

    public Direction getHitFace() {
        return hitFace;
    }

    public int getPlaceX() {
        return blockX + hitFace.dx();
    }

    public int getPlaceY() {
        return blockY + hitFace.dy();
    }

    public int getPlaceZ() {
        return blockZ + hitFace.dz();
    }
}