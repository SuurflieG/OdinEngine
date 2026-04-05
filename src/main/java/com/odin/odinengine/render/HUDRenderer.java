package com.odin.odinengine.render;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
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

    private int crosshairVaoId;
    private int crosshairVboId;

    private int panelVaoId;
    private int panelVboId;

    private TTFTextRenderer textRenderer;

    public void init() {
        hudShader = new Shader(
                "src/main/resources/shaders/hud_vertex.glsl",
                "src/main/resources/shaders/hud_fragment.glsl"
        );

        textRenderer = new TTFTextRenderer();
        textRenderer.init("src/main/resources/assets/odinengine/fonts/rainyhearts.ttf", 32.0f);

        initCrosshair();
        initPanel();
    }

    private void initCrosshair() {
        float size = 0.015f;
        float[] vertices = {
                -size, 0.0f,
                size, 0.0f,
                0.0f, -size,
                0.0f,  size
        };

        crosshairVaoId = glGenVertexArrays();
        crosshairVboId = glGenBuffers();

        glBindVertexArray(crosshairVaoId);

        FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(vertices.length);
        vertexBuffer.put(vertices).flip();

        glBindBuffer(GL_ARRAY_BUFFER, crosshairVboId);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0L);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        MemoryUtil.memFree(vertexBuffer);
    }

    private void initPanel() {
        panelVaoId = glGenVertexArrays();
        panelVboId = glGenBuffers();

        glBindVertexArray(panelVaoId);
        glBindBuffer(GL_ARRAY_BUFFER, panelVboId);

        // 6 vertices, 2 floats each, dynamic because panel is rebuilt each frame
        glBufferData(GL_ARRAY_BUFFER, 6L * 2L * Float.BYTES, GL_DYNAMIC_DRAW);

        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0L);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void render(int screenWidth, int screenHeight, String targetedBlockName) {
        glDisable(GL_DEPTH_TEST);

        drawCrosshair();

        if (targetedBlockName != null && !targetedBlockName.isBlank()) {
            float panelWidth = 220.0f;
            float panelHeight = 36.0f;

            float panelX = (screenWidth - panelWidth) / 2.0f;
            float panelY = 20.0f;

            drawPanel(panelX, panelY, panelWidth, panelHeight, screenWidth, screenHeight);

            // approximate centering for now
            float textWidthEstimate = targetedBlockName.length() * 14.0f;
            float textX = panelX + (panelWidth - textWidthEstimate) / 2.0f;
            float textY = panelY + 25.0f;

            textRenderer.renderText(targetedBlockName, textX, textY, screenWidth, screenHeight);
        }

        glEnable(GL_DEPTH_TEST);
    }

    private void drawCrosshair() {
        hudShader.bind();
        hudShader.setUniform("hudColor", 1.0f, 1.0f, 1.0f);

        glBindVertexArray(crosshairVaoId);
        glDrawArrays(GL_LINES, 0, 4);
        glBindVertexArray(0);

        hudShader.unbind();
    }

    private void drawPanel(float x, float y, float width, float height, int screenWidth, int screenHeight) {
        float left = pixelToNdcX(x, screenWidth);
        float right = pixelToNdcX(x + width, screenWidth);
        float top = pixelToNdcY(y, screenHeight);
        float bottom = pixelToNdcY(y + height, screenHeight);

        float[] vertices = {
                left,  top,
                right, top,
                right, bottom,

                right, bottom,
                left,  bottom,
                left,  top
        };

        FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(vertices.length);
        vertexBuffer.put(vertices).flip();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        hudShader.bind();
        hudShader.setUniform("hudColor", 0.0f, 0.0f, 0.0f, 0.65f);

        glBindVertexArray(panelVaoId);
        glBindBuffer(GL_ARRAY_BUFFER, panelVboId);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_DYNAMIC_DRAW);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        hudShader.unbind();

        glDisable(GL_BLEND);
        MemoryUtil.memFree(vertexBuffer);
    }

    private float pixelToNdcX(float x, int screenWidth) {
        return (x / screenWidth) * 2.0f - 1.0f;
    }

    private float pixelToNdcY(float y, int screenHeight) {
        return 1.0f - (y / screenHeight) * 2.0f;
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

        if (panelVboId != 0) {
            glDeleteBuffers(panelVboId);
            panelVboId = 0;
        }

        if (panelVaoId != 0) {
            glDeleteVertexArrays(panelVaoId);
            panelVaoId = 0;
        }

        if (crosshairVboId != 0) {
            glDeleteBuffers(crosshairVboId);
            crosshairVboId = 0;
        }

        if (crosshairVaoId != 0) {
            glDeleteVertexArrays(crosshairVaoId);
            crosshairVaoId = 0;
        }
    }
}