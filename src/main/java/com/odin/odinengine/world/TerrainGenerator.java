package com.odin.odinengine.world;

public class TerrainGenerator {

    public int getHeight(int worldX, int worldZ) {
        double largeWaves = Math.sin(worldX * 0.08) * 2.5 + Math.cos(worldZ * 0.08) * 2.5;
        double mediumWaves = Math.sin(worldX * 0.18) * 1.5 + Math.cos(worldZ * 0.18) * 1.5;
        double smallVariation = Math.sin((worldX + worldZ) * 0.35) * 0.75;

        double heightValue = 6.0 + largeWaves + mediumWaves + smallVariation;

        int height = (int) Math.floor(heightValue);

        if (height < 1) {
            height = 1;
        }
        if (height >= Chunk.SIZE_Y) {
            height = Chunk.SIZE_Y - 1;
        }

        return height;
    }
}