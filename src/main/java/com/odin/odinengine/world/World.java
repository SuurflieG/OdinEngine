package com.odin.odinengine.world;

import org.joml.Vector3f;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class World {

    private final Map<ChunkPos, Chunk> chunks = new LinkedHashMap<>();
    private final TerrainGenerator terrainGenerator = new TerrainGenerator();
    private final BlockRegistry blockRegistry;

    public World(BlockRegistry blockRegistry) {
        this.blockRegistry = blockRegistry;
    }

    public void generateTestWorld(int radius) {
        chunks.clear();
        ensureChunksInRadius(0, 0, radius);
    }

    public void ensureChunksInRadius(int centerChunkX, int centerChunkZ, int radius) {
        for (int chunkX = centerChunkX - radius; chunkX <= centerChunkX + radius; chunkX++) {
            for (int chunkZ = centerChunkZ - radius; chunkZ <= centerChunkZ + radius; chunkZ++) {
                ChunkPos pos = new ChunkPos(chunkX, chunkZ);
                chunks.computeIfAbsent(pos, p -> new Chunk(p, terrainGenerator, blockRegistry));
            }
        }
    }

    public void unloadChunksOutsideRadius(int centerChunkX, int centerChunkZ, int radius) {
        Iterator<Map.Entry<ChunkPos, Chunk>> iterator = chunks.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<ChunkPos, Chunk> entry = iterator.next();
            ChunkPos pos = entry.getKey();

            if (Math.abs(pos.x() - centerChunkX) > radius || Math.abs(pos.z() - centerChunkZ) > radius) {
                iterator.remove();
            }
        }
    }

    public RaycastHit raycast(Vector3f origin, Vector3f direction, float maxDistance, float stepSize) {
        Vector3f current = new Vector3f(origin);

        int previousBlockX = (int) Math.floor(current.x);
        int previousBlockY = (int) Math.floor(current.y);
        int previousBlockZ = (int) Math.floor(current.z);

        for (float traveled = 0.0f; traveled <= maxDistance; traveled += stepSize) {
            int blockX = (int) Math.floor(current.x);
            int blockY = (int) Math.floor(current.y);
            int blockZ = (int) Math.floor(current.z);

            short blockId = getBlockId(blockX, blockY, blockZ);

            if (blockRegistry.isSolid(blockId)) {
                return new RaycastHit(
                        blockX, blockY, blockZ, blockId,
                        previousBlockX, previousBlockY, previousBlockZ
                );
            }

            previousBlockX = blockX;
            previousBlockY = blockY;
            previousBlockZ = blockZ;

            current.fma(stepSize, direction);
        }

        return null;
    }

    public void setBlockId(int worldX, int y, int worldZ, short blockId) {
        if (y < 0 || y >= Chunk.SIZE_Y) {
            return;
        }

        int chunkX = Math.floorDiv(worldX, Chunk.SIZE_X);
        int chunkZ = Math.floorDiv(worldZ, Chunk.SIZE_Z);

        int localX = Math.floorMod(worldX, Chunk.SIZE_X);
        int localZ = Math.floorMod(worldZ, Chunk.SIZE_Z);

        Chunk chunk = chunks.get(new ChunkPos(chunkX, chunkZ));
        if (chunk == null) {
            return;
        }

        chunk.setBlockId(localX, y, localZ, blockId);
    }

    public boolean isBlockSolid(int worldX, int y, int worldZ) {
        return blockRegistry.isSolid(getBlockId(worldX, y, worldZ));
    }

    public ChunkPos getChunkPosFromWorldPos(int worldX, int worldZ) {
        int chunkX = Math.floorDiv(worldX, Chunk.SIZE_X);
        int chunkZ = Math.floorDiv(worldZ, Chunk.SIZE_Z);
        return new ChunkPos(chunkX, chunkZ);
    }

    public Collection<Chunk> getChunks() {
        return chunks.values();
    }

    public Chunk getChunk(ChunkPos pos) {
        return chunks.get(pos);
    }

    public short getBlockId(int worldX, int y, int worldZ) {
        if (y < 0 || y >= Chunk.SIZE_Y) {
            return BlockRegistry.AIR_ID;
        }

        int chunkX = Math.floorDiv(worldX, Chunk.SIZE_X);
        int chunkZ = Math.floorDiv(worldZ, Chunk.SIZE_Z);

        int localX = Math.floorMod(worldX, Chunk.SIZE_X);
        int localZ = Math.floorMod(worldZ, Chunk.SIZE_Z);

        Chunk chunk = chunks.get(new ChunkPos(chunkX, chunkZ));
        if (chunk == null) {
            return BlockRegistry.AIR_ID;
        }

        return chunk.getBlockId(localX, y, localZ);
    }

    public int getLoadedChunkCount() {
        return chunks.size();
    }

    public BlockRegistry getBlockRegistry() {
        return blockRegistry;
    }
}