package com.odin.odinengine.world;

import java.util.Objects;

public class ChunkPos {

    private final int x;
    private final int z;

    public ChunkPos(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int x() {
        return x;
    }

    public int z() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChunkPos chunkPos)) return false;
        return x == chunkPos.x && z == chunkPos.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }
}