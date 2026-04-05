package com.odin.odinengine.world;

public class Chunk {

    public static final int SIZE_X = 16;
    public static final int SIZE_Y = 16;
    public static final int SIZE_Z = 16;

    private final ChunkPos chunkPos;
    private final short[][][] blocks;

    public Chunk(ChunkPos chunkPos, TerrainGenerator terrainGenerator, BlockRegistry blockRegistry) {
        this.chunkPos = chunkPos;
        this.blocks = new short[SIZE_X][SIZE_Y][SIZE_Z];
        generateTerrain(terrainGenerator, blockRegistry);
    }

    private void generateTerrain(TerrainGenerator terrainGenerator, BlockRegistry blockRegistry) {
        short air = BlockRegistry.AIR_ID;
        short grass = blockRegistry.getId("grass");
        short dirt = blockRegistry.getId("dirt");
        short stone = blockRegistry.getId("stone");

        for (int localX = 0; localX < SIZE_X; localX++) {
            for (int localZ = 0; localZ < SIZE_Z; localZ++) {
                int worldX = chunkPos.x() * SIZE_X + localX;
                int worldZ = chunkPos.z() * SIZE_Z + localZ;

                int surfaceHeight = terrainGenerator.getHeight(worldX, worldZ);

                for (int y = 0; y < SIZE_Y; y++) {
                    if (y > surfaceHeight) {
                        blocks[localX][y][localZ] = air;
                    } else if (y == surfaceHeight) {
                        blocks[localX][y][localZ] = grass;
                    } else if (y >= surfaceHeight - 2) {
                        blocks[localX][y][localZ] = dirt;
                    } else {
                        blocks[localX][y][localZ] = stone;
                    }
                }
            }
        }
    }

    public short getBlockId(int x, int y, int z) {
        if (x < 0 || x >= SIZE_X || y < 0 || y >= SIZE_Y || z < 0 || z >= SIZE_Z) {
            return BlockRegistry.AIR_ID;
        }
        return blocks[x][y][z];
    }

    public void setBlockId(int x, int y, int z, short blockId) {
        if (x < 0 || x >= SIZE_X || y < 0 || y >= SIZE_Y || z < 0 || z >= SIZE_Z) {
            return;
        }
        blocks[x][y][z] = blockId;
    }

    public ChunkPos getChunkPos() {
        return chunkPos;
    }
}