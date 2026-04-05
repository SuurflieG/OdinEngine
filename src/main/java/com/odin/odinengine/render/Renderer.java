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
    private DebugLineRenderer debugLineRenderer;

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

        debugLineRenderer = new DebugLineRenderer();
        debugLineRenderer.init();

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
            int screenWidth,
            int screenHeight,
            Vector3f debugRayStart,
            Vector3f debugRayEnd
    ) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        shader.bind();
        shader.setUniform("projection", projection);
        shader.setUniform("view", camera.getViewMatrix());
        chunkRenderer.render(shader, world);
        shader.unbind();

        if (debugRayStart != null && debugRayEnd != null) {
            debugLineRenderer.renderLine(debugRayStart, debugRayEnd, projection, camera.getViewMatrix());
        }

        hudRenderer.render(screenWidth, screenHeight, targetedBlockName);
    }

    public void renderDebugRay(Vector3f start, Vector3f end, Camera camera) {
        if (debugLineRenderer != null) {
            debugLineRenderer.renderLine(start, end, projection, camera.getViewMatrix());
        }
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
        if (debugLineRenderer != null) {
            debugLineRenderer.cleanup();
        }
    }

}