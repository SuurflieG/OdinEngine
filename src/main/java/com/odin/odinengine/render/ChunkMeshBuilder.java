package com.odin.odinengine.render;

import com.odin.odinengine.world.BlockDefinition;
import com.odin.odinengine.world.BlockRegistry;
import com.odin.odinengine.world.Chunk;
import com.odin.odinengine.world.Direction;
import com.odin.odinengine.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChunkMeshBuilder {

    public List<ChunkMeshData> build(World world, Chunk chunk) {
        BlockRegistry registry = world.getBlockRegistry();

        Map<MeshKey, List<Float>> verticesByKey = new HashMap<>();
        Map<MeshKey, List<Integer>> indicesByKey = new HashMap<>();
        Map<MeshKey, Integer> vertexIndexByKey = new HashMap<>();
        Map<MeshKey, Integer> faceCountByKey = new HashMap<>();

        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int y = 0; y < Chunk.SIZE_Y; y++) {
                for (int z = 0; z < Chunk.SIZE_Z; z++) {
                    short blockId = chunk.getBlockId(x, y, z);
                    BlockDefinition block = registry.get(blockId);

                    if (!block.isSolid()) {
                        continue;
                    }

                    for (Direction direction : Direction.values()) {
                        if (isFaceExposed(world, chunk, x, y, z, direction)) {
                            MeshKey key = new MeshKey(blockId, direction);

                            verticesByKey.computeIfAbsent(key, k -> new ArrayList<>());
                            indicesByKey.computeIfAbsent(key, k -> new ArrayList<>());
                            vertexIndexByKey.putIfAbsent(key, 0);
                            faceCountByKey.put(key, faceCountByKey.getOrDefault(key, 0) + 1);

                            int vertexIndex = vertexIndexByKey.get(key);

                            addFace(
                                    verticesByKey.get(key),
                                    indicesByKey.get(key),
                                    x, y, z,
                                    direction,
                                    vertexIndex
                            );

                            vertexIndexByKey.put(key, vertexIndex + 4);
                        }
                    }
                }
            }
        }

        List<ChunkMeshData> result = new ArrayList<>();

        for (Map.Entry<MeshKey, List<Float>> entry : verticesByKey.entrySet()) {
            MeshKey key = entry.getKey();

            result.add(new ChunkMeshData(
                    key.blockId(),
                    key.direction(),
                    toFloatArray(verticesByKey.get(key)),
                    toIntArray(indicesByKey.get(key)),
                    faceCountByKey.getOrDefault(key, 0)
            ));
        }

        return result;
    }

    private boolean isFaceExposed(World world, Chunk chunk, int x, int y, int z, Direction direction) {
        int worldX = chunk.getChunkPos().x() * Chunk.SIZE_X + x;
        int worldZ = chunk.getChunkPos().z() * Chunk.SIZE_Z + z;

        short neighborId = world.getBlockId(
                worldX + direction.dx(),
                y + direction.dy(),
                worldZ + direction.dz()
        );

        return !world.getBlockRegistry().isSolid(neighborId);
    }

    private void addFace(List<Float> vertices, List<Integer> indices,
                         int x, int y, int z,
                         Direction direction,
                         int vertexIndex) {

        float[][] faceVertices = getFaceVertices(x, y, z, direction);
        float[][] uvs = getFaceUVs();

        for (int i = 0; i < 4; i++) {
            vertices.add(faceVertices[i][0]);
            vertices.add(faceVertices[i][1]);
            vertices.add(faceVertices[i][2]);

            vertices.add(uvs[i][0]);
            vertices.add(uvs[i][1]);
        }

        indices.add(vertexIndex);
        indices.add(vertexIndex + 1);
        indices.add(vertexIndex + 2);
        indices.add(vertexIndex + 2);
        indices.add(vertexIndex + 3);
        indices.add(vertexIndex);
    }

    private float[][] getFaceUVs() {
        return new float[][]{
                {0.0f, 1.0f},
                {1.0f, 1.0f},
                {1.0f, 0.0f},
                {0.0f, 0.0f}
        };
    }

    private float[][] getFaceVertices(int x, int y, int z, Direction direction) {
        float minX = x - 0.5f;
        float maxX = x + 0.5f;
        float minY = y - 0.5f;
        float maxY = y + 0.5f;
        float minZ = z - 0.5f;
        float maxZ = z + 0.5f;

        return switch (direction) {
            case FRONT -> new float[][]{
                    {minX, minY, maxZ},
                    {maxX, minY, maxZ},
                    {maxX, maxY, maxZ},
                    {minX, maxY, maxZ}
            };
            case BACK -> new float[][]{
                    {maxX, minY, minZ},
                    {minX, minY, minZ},
                    {minX, maxY, minZ},
                    {maxX, maxY, minZ}
            };
            case LEFT -> new float[][]{
                    {minX, minY, minZ},
                    {minX, minY, maxZ},
                    {minX, maxY, maxZ},
                    {minX, maxY, minZ}
            };
            case RIGHT -> new float[][]{
                    {maxX, minY, maxZ},
                    {maxX, minY, minZ},
                    {maxX, maxY, minZ},
                    {maxX, maxY, maxZ}
            };
            case TOP -> new float[][]{
                    {minX, maxY, maxZ},
                    {maxX, maxY, maxZ},
                    {maxX, maxY, minZ},
                    {minX, maxY, minZ}
            };
            case BOTTOM -> new float[][]{
                    {minX, minY, minZ},
                    {maxX, minY, minZ},
                    {maxX, minY, maxZ},
                    {minX, minY, maxZ}
            };
        };
    }

    private float[] toFloatArray(List<Float> list) {
        float[] array = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    private int[] toIntArray(List<Integer> list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    private record MeshKey(short blockId, Direction direction) {}
}