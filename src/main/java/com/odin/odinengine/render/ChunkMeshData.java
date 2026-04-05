package com.odin.odinengine.render;

import com.odin.odinengine.world.Direction;

public class ChunkMeshData {

    private final short blockId;
    private final Direction direction;
    private final float[] vertices;
    private final int[] indices;
    private final int faceCount;

    public ChunkMeshData(short blockId, Direction direction, float[] vertices, int[] indices, int faceCount) {
        this.blockId = blockId;
        this.direction = direction;
        this.vertices = vertices;
        this.indices = indices;
        this.faceCount = faceCount;
    }

    public short getBlockId() {
        return blockId;
    }

    public Direction getDirection() {
        return direction;
    }

    public float[] getVertices() {
        return vertices;
    }

    public int[] getIndices() {
        return indices;
    }

    public int getFaceCount() {
        return faceCount;
    }
}