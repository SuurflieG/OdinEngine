package com.odin.odinengine.render;

import com.odin.odinengine.world.Direction;

public class ChunkTextureMesh {

    private final short blockId;
    private final Direction direction;
    private final Mesh mesh;

    public ChunkTextureMesh(short blockId, Direction direction, Mesh mesh) {
        this.blockId = blockId;
        this.direction = direction;
        this.mesh = mesh;
    }

    public short getBlockId() {
        return blockId;
    }

    public Direction getDirection() {
        return direction;
    }

    public Mesh getMesh() {
        return mesh;
    }
}