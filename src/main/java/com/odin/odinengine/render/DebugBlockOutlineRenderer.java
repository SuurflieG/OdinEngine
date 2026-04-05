package com.odin.odinengine.render;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class DebugBlockOutlineRenderer {
    private Shader shader;
    private int vaoId;
    private int vboId;

    public void init() {
        shader = new Shader(
                "src/main/resources/shaders/debug_line_vertex.glsl",
                "src/main/resources/shaders/debug_line_fragment.glsl"
        );

        vaoId = glGenVertexArrays();
        vboId = glGenBuffers();

        glBindVertexArray(vaoId);
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, 24L * 3L * Float.BYTES, GL_DYNAMIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0L);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void renderBlockOutline(int blockX, int blockY, int blockZ, Matrix4f projection, Matrix4f view) {
        float e = 0.002f;
        renderBox(
                blockX - e, blockY - e, blockZ - e,
                blockX + 1.0f + e, blockY + 1.0f + e, blockZ + 1.0f + e,
                projection, view,
                1.0f, 0.0f, 0.0f,
                false
        );
    }

    public void renderBox(
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            Matrix4f projection,
            Matrix4f view,
            float r, float g, float b,
            boolean depthTest
    ) {
        float[] vertices = {
                x0, y0, z0,  x1, y0, z0,
                x1, y0, z0,  x1, y0, z1,
                x1, y0, z1,  x0, y0, z1,
                x0, y0, z1,  x0, y0, z0,

                x0, y1, z0,  x1, y1, z0,
                x1, y1, z0,  x1, y1, z1,
                x1, y1, z1,  x0, y1, z1,
                x0, y1, z1,  x0, y1, z0,

                x0, y0, z0,  x0, y1, z0,
                x1, y0, z0,  x1, y1, z0,
                x1, y0, z1,  x1, y1, z1,
                x0, y0, z1,  x0, y1, z1
        };

        FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(vertices.length);
        vertexBuffer.put(vertices).flip();

        if (depthTest) {
            glEnable(GL_DEPTH_TEST);
        } else {
            glDisable(GL_DEPTH_TEST);
        }

        glLineWidth(2.5f);

        shader.bind();
        shader.setUniform("projection", projection);
        shader.setUniform("view", view);
        shader.setUniform("lineColor", r, g, b);

        glBindVertexArray(vaoId);
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_DYNAMIC_DRAW);
        glDrawArrays(GL_LINES, 0, 24);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        shader.unbind();
        glEnable(GL_DEPTH_TEST);

        MemoryUtil.memFree(vertexBuffer);
    }

    public void cleanup() {
        if (shader != null) {
            shader.cleanup();
            shader = null;
        }
        if (vboId != 0) {
            glDeleteBuffers(vboId);
            vboId = 0;
        }
        if (vaoId != 0) {
            glDeleteVertexArrays(vaoId);
            vaoId = 0;
        }
    }
}