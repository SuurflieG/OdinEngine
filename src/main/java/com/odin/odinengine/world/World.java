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

    public RaycastHit raycast(Vector3f origin, Vector3f direction, float maxDistance) {
        Vector3f dir = new Vector3f(direction).normalize();

        int x = (int) Math.floor(origin.x);
        int y = (int) Math.floor(origin.y);
        int z = (int) Math.floor(origin.z);

        int stepX = dir.x > 0.0f ? 1 : (dir.x < 0.0f ? -1 : 0);
        int stepY = dir.y > 0.0f ? 1 : (dir.y < 0.0f ? -1 : 0);
        int stepZ = dir.z > 0.0f ? 1 : (dir.z < 0.0f ? -1 : 0);

        float tDeltaX = stepX != 0 ? Math.abs(1.0f / dir.x) : Float.POSITIVE_INFINITY;
        float tDeltaY = stepY != 0 ? Math.abs(1.0f / dir.y) : Float.POSITIVE_INFINITY;
        float tDeltaZ = stepZ != 0 ? Math.abs(1.0f / dir.z) : Float.POSITIVE_INFINITY;

        float tMaxX = intBound(origin.x, dir.x);
        float tMaxY = intBound(origin.y, dir.y);
        float tMaxZ = intBound(origin.z, dir.z);

        Direction enteredFace = null;
        float traveled = 0.0f;

        while (traveled <= maxDistance) {
            short blockId = getBlockId(x, y, z);
            if (blockRegistry.isSolid(blockId)) {
                float hitX = origin.x + dir.x * traveled;
                float hitY = origin.y + dir.y * traveled;
                float hitZ = origin.z + dir.z * traveled;

                return new RaycastHit(x, y, z, blockId, enteredFace != null ? enteredFace : Direction.FRONT, hitX, hitY, hitZ
                );
            }

            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    x += stepX;
                    traveled = tMaxX;
                    tMaxX += tDeltaX;
                    enteredFace = stepX > 0 ? Direction.LEFT : Direction.RIGHT;
                } else {
                    z += stepZ;
                    traveled = tMaxZ;
                    tMaxZ += tDeltaZ;
                    enteredFace = stepZ > 0 ? Direction.BACK : Direction.FRONT;
                }
            } else {
                if (tMaxY < tMaxZ) {
                    y += stepY;
                    traveled = tMaxY;
                    tMaxY += tDeltaY;
                    enteredFace = stepY > 0 ? Direction.BOTTOM : Direction.TOP;
                } else {
                    z += stepZ;
                    traveled = tMaxZ;
                    tMaxZ += tDeltaZ;
                    enteredFace = stepZ > 0 ? Direction.BACK : Direction.FRONT;
                }
            }
        }

        return null;
    }

    private float intBound(float s, float ds) {
        if (ds == 0.0f) {
            return Float.POSITIVE_INFINITY;
        }

        if (ds > 0.0f) {
            return ((float) Math.floor(s) + 1.0f - s) / ds;
        } else {
            return (s - (float) Math.floor(s)) / -ds;
        }
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