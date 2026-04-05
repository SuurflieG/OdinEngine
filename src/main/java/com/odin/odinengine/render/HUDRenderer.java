package com.odin.odinengine.render;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class HUDRenderer {
    private Shader hudShader;
    private int vaoId;
    private int vboId;

    private TTFTextRenderer textRenderer;

    public void init() {
        hudShader = new Shader(
                "src/main/resources/shaders/hud_vertex.glsl",
                "src/main/resources/shaders/hud_fragment.glsl"
        );

        textRenderer = new TTFTextRenderer();
        textRenderer.init("src/main/resources/assets/odinengine/fonts/rainyhearts.ttf", 24.0f);

        float size = 0.015f;
        float[] vertices = {
                -size, 0.0f,
                size, 0.0f,
                0.0f, -size,
                0.0f,  size
        };

        vaoId = glGenVertexArrays();
        vboId = glGenBuffers();

        glBindVertexArray(vaoId);

        FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(vertices.length);
        vertexBuffer.put(vertices).flip();

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0L);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        MemoryUtil.memFree(vertexBuffer);
    }

    public void render(int screenWidth, int screenHeight, String targetedBlockName) {
        glDisable(GL_DEPTH_TEST);

        hudShader.bind();
        hudShader.setUniform("hudColor", 1.0f, 1.0f, 1.0f);

        glBindVertexArray(vaoId);
        glDrawArrays(GL_LINES, 0, 4);
        glBindVertexArray(0);

        hudShader.unbind();

        String textToShow = (targetedBlockName != null && !targetedBlockName.isBlank())
                ? targetedBlockName
                : "TEST";

        float textX = (screenWidth / 2.0f) + 14.0f;
        float textY = (screenHeight / 2.0f) - 8.0f;

        textRenderer.renderText(textToShow, textX, textY, screenWidth, screenHeight);

        glEnable(GL_DEPTH_TEST);
    }

    public void cleanup() {
        if (hudShader != null) {
            hudShader.cleanup();
            hudShader = null;
        }

        if (textRenderer != null) {
            textRenderer.cleanup();
            textRenderer = null;
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