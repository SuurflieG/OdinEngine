package com.odin.odinengine.render;

import com.odin.odinengine.world.BlockDefinition;
import com.odin.odinengine.world.Chunk;
import com.odin.odinengine.world.ChunkPos;
import com.odin.odinengine.world.Direction;
import com.odin.odinengine.world.World;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChunkRenderer {

    private final Map<ChunkPos, ChunkRenderEntry> chunkEntries = new HashMap<>();
    private final Map<String, Texture> texturesByPath = new HashMap<>();

    public void buildWorldMeshes(World world) {
        cleanupMeshesOnly();
        chunkEntries.clear();

        for (Chunk chunk : world.getChunks()) {
            rebuildChunkMesh(world, chunk.getChunkPos());
        }
    }

    public void rebuildChunkMesh(World world, ChunkPos chunkPos) {
        Chunk chunk = world.getChunk(chunkPos);
        if (chunk == null) {
            removeChunkMesh(chunkPos);
            return;
        }

        removeChunkMesh(chunkPos);

        ChunkMeshBuilder meshBuilder = new ChunkMeshBuilder();
        List<ChunkMeshData> meshDataList = meshBuilder.build(world, chunk);
        List<ChunkTextureMesh> textureMeshes = new ArrayList<>();

        int faceCount = 0;

        for (ChunkMeshData meshData : meshDataList) {
            BlockDefinition block = world.getBlockRegistry().get(meshData.getBlockId());
            String texturePath = block.getTexturePath(meshData.getDirection());

            ensureTextureLoaded(block.getName(), meshData.getDirection(), texturePath);

            Mesh mesh = new Mesh(meshData.getVertices(), meshData.getIndices());
            textureMeshes.add(new ChunkTextureMesh(meshData.getBlockId(), meshData.getDirection(), mesh));
            faceCount += meshData.getFaceCount();
        }

        int blockCount = countSolidBlocks(chunk, world);

        chunkEntries.put(
                chunkPos,
                new ChunkRenderEntry(chunkPos, textureMeshes, blockCount, faceCount)
        );
    }

    public void removeChunkMesh(ChunkPos chunkPos) {
        ChunkRenderEntry existing = chunkEntries.remove(chunkPos);
        if (existing != null) {
            for (ChunkTextureMesh textureMesh : existing.getTextureMeshes()) {
                textureMesh.getMesh().cleanup();
            }
        }
    }

    private void ensureTextureLoaded(String blockName, Direction direction, String texturePath) {
        if (texturePath == null || texturePath.isBlank()) {
            throw new IllegalStateException("No texture path for block '" + blockName + "' face '" + direction + "'");
        }

        texturesByPath.computeIfAbsent(texturePath, path -> {
            System.out.println("Loading texture for block face: " + blockName + " [" + direction + "] -> " + path);
            return new Texture(path);
        });
    }

    public void render(Shader shader, World world) {
        Matrix4f model = new Matrix4f();
        shader.setUniform("blockTexture", 0);

        for (ChunkRenderEntry entry : chunkEntries.values()) {
            ChunkPos pos = entry.getChunkPos();

            float worldX = pos.x() * Chunk.SIZE_X;
            float worldZ = pos.z() * Chunk.SIZE_Z;

            model.identity().translate(worldX, 0.0f, worldZ);
            shader.setUniform("model", model);

            for (ChunkTextureMesh textureMesh : entry.getTextureMeshes()) {
                BlockDefinition block = world.getBlockRegistry().get(textureMesh.getBlockId());
                String texturePath = block.getTexturePath(textureMesh.getDirection());

                Texture texture = texturesByPath.get(texturePath);
                if (texture != null) {
                    texture.bind();
                }

                textureMesh.getMesh().render();
            }
        }
    }

    private int countSolidBlocks(Chunk chunk, World world) {
        int count = 0;

        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int y = 0; y < Chunk.SIZE_Y; y++) {
                for (int z = 0; z < Chunk.SIZE_Z; z++) {
                    short blockId = chunk.getBlockId(x, y, z);
                    if (world.getBlockRegistry().isSolid(blockId)) {
                        count++;
                    }
                }
            }
        }

        return count;
    }

    private void cleanupMeshesOnly() {
        for (ChunkRenderEntry entry : chunkEntries.values()) {
            for (ChunkTextureMesh textureMesh : entry.getTextureMeshes()) {
                textureMesh.getMesh().cleanup();
            }
        }
    }

    public int getRenderedBlockCount() {
        int total = 0;
        for (ChunkRenderEntry entry : chunkEntries.values()) {
            total += entry.getBlockCount();
        }
        return total;
    }

    public int getRenderedFaceCount() {
        int total = 0;
        for (ChunkRenderEntry entry : chunkEntries.values()) {
            total += entry.getFaceCount();
        }
        return total;
    }

    public void cleanup() {
        cleanupMeshesOnly();
        chunkEntries.clear();

        for (Texture texture : texturesByPath.values()) {
            texture.cleanup();
        }
        texturesByPath.clear();
    }
}