package com.odin.odinengine.render;

import com.odin.odinengine.world.ChunkPos;

import java.util.List;

public class ChunkRenderEntry {

    private final ChunkPos chunkPos;
    private final List<ChunkTextureMesh> textureMeshes;
    private final int blockCount;
    private final int faceCount;

    public ChunkRenderEntry(ChunkPos chunkPos, List<ChunkTextureMesh> textureMeshes, int blockCount, int faceCount) {
        this.chunkPos = chunkPos;
        this.textureMeshes = textureMeshes;
        this.blockCount = blockCount;
        this.faceCount = faceCount;
    }

    public ChunkPos getChunkPos() {
        return chunkPos;
    }

    public List<ChunkTextureMesh> getTextureMeshes() {
        return textureMeshes;
    }

    public int getBlockCount() {
        return blockCount;
    }

    public int getFaceCount() {
        return faceCount;
    }
}