package com.odin.odinengine.render;

import com.odin.odinengine.math.Camera;
import com.odin.odinengine.world.ChunkPos;
import com.odin.odinengine.world.World;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Collection;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

public class Renderer {

    private Shader shader;
    private ChunkRenderer chunkRenderer;
    private HUDRenderer hudRenderer;
    private DebugBlockOutlineRenderer debugBlockOutlineRenderer;

    private final Matrix4f projection = new Matrix4f();

    public void init(int width, int height, World world) {
        shader = new Shader(
                "src/main/resources/shaders/vertex.glsl",
                "src/main/resources/shaders/fragment.glsl"
        );

        chunkRenderer = new ChunkRenderer();
        chunkRenderer.buildWorldMeshes(world);

        hudRenderer = new HUDRenderer();
        hudRenderer.init();

        debugBlockOutlineRenderer = new DebugBlockOutlineRenderer();
        debugBlockOutlineRenderer.init();

        float aspectRatio = (float) width / (float) height;
        projection.setPerspective((float) Math.toRadians(70.0f), aspectRatio, 0.1f, 1000.0f);
    }

    public void rebuildWorldMeshes(World world) {
        if (chunkRenderer != null) {
            chunkRenderer.buildWorldMeshes(world);
        }
    }

    public void rebuildChunk(World world, ChunkPos chunkPos) {
        if (chunkRenderer != null) {
            chunkRenderer.rebuildChunkMesh(world, chunkPos);
        }
    }

    public void rebuildChunks(World world, Collection<ChunkPos> chunkPositions) {
        if (chunkRenderer != null) {
            for (ChunkPos chunkPos : chunkPositions) {
                chunkRenderer.rebuildChunkMesh(world, chunkPos);
            }
        }
    }

    public void removeChunkMesh(ChunkPos chunkPos) {
        if (chunkRenderer != null) {
            chunkRenderer.removeChunkMesh(chunkPos);
        }
    }

    public void render(
            Camera camera,
            World world,
            String targetedBlockName,
            String selectedBlockName,
            int screenWidth,
            int screenHeight,
            boolean showBlockOutline,
            int hitBlockX,
            int hitBlockY,
            int hitBlockZ
    ) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        shader.bind();
        shader.setUniform("projection", projection);
        shader.setUniform("view", camera.getViewMatrix());
        chunkRenderer.render(shader, world);
        shader.unbind();

        if (showBlockOutline) {
            debugBlockOutlineRenderer.renderBlockOutline(
                    hitBlockX,
                    hitBlockY,
                    hitBlockZ,
                    projection,
                    camera.getViewMatrix()
            );
        }

        hudRenderer.render(screenWidth, screenHeight, targetedBlockName, selectedBlockName);
    }

    public int getRenderedBlockCount() {
        return chunkRenderer != null ? chunkRenderer.getRenderedBlockCount() : 0;
    }

    public int getRenderedFaceCount() {
        return chunkRenderer != null ? chunkRenderer.getRenderedFaceCount() : 0;
    }

    public void cleanup() {
        if (chunkRenderer != null) {
            chunkRenderer.cleanup();
        }
        if (hudRenderer != null) {
            hudRenderer.cleanup();
        }
        if (shader != null) {
            shader.cleanup();
        }
        if (debugBlockOutlineRenderer != null) {
            debugBlockOutlineRenderer.cleanup();
        }
    }

}