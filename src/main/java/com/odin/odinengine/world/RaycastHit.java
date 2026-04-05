package com.odin.odinengine.world;

public class RaycastHit {
    private final int blockX;
    private final int blockY;
    private final int blockZ;
    private final short blockId;
    private final Direction hitFace;

    private final float hitX;
    private final float hitY;
    private final float hitZ;

    public RaycastHit(
            int blockX,
            int blockY,
            int blockZ,
            short blockId,
            Direction hitFace,
            float hitX,
            float hitY,
            float hitZ
    ) {
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
        this.blockId = blockId;
        this.hitFace = hitFace;
        this.hitX = hitX;
        this.hitY = hitY;
        this.hitZ = hitZ;
    }

    public int getBlockX() { return blockX; }
    public int getBlockY() { return blockY; }
    public int getBlockZ() { return blockZ; }
    public short getBlockId() { return blockId; }
    public Direction getHitFace() { return hitFace; }

    public float getHitX() { return hitX; }
    public float getHitY() { return hitY; }
    public float getHitZ() { return hitZ; }

    public int getPlaceX() { return blockX + hitFace.dx(); }
    public int getPlaceY() { return blockY + hitFace.dy(); }
    public int getPlaceZ() { return blockZ + hitFace.dz(); }
}