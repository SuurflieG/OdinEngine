package com.odin.odinengine.world;

public enum BlockType {
    AIR(false),
    GRASS(true),
    DIRT(true),
    STONE(true);

    private final boolean solid;

    BlockType(boolean solid) {
        this.solid = solid;
    }

    public boolean isSolid() {
        return solid;
    }
}