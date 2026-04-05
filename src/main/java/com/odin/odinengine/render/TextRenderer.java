package com.odin.odinengine.render;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL30.*;

public class TextRenderer {

    private Shader textShader;
    private FontTexture fontTexture;

    public void init() {
        textShader = new Shader(
                "src/main/resources/shaders/text_vertex.glsl",
                "src/main/resources/shaders/text_fragment.glsl"
        );

        fontTexture = new FontTexture(
                "assets/odinengine/textures/ui/font_old.png",
                16,
                16
        );
    }

    public void renderText(String text, float startX, float startY, float scale, int screenWidth, int screenHeight) {
        if (text == null || text.isEmpty()) {
            return;
        }

        List<Float> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        float x = startX;
        float y = startY;
        int vertexIndex = 0;

        float charWidth = 8.0f * scale;
        float charHeight = 8.0f * scale;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '\n') {
                x = startX;
                y += charHeight;
                continue;
            }

            int code = c;
            float u0 = fontTexture.getU0(code);
            float v0 = fontTexture.getV0(code);
            float u1 = fontTexture.getU1(code);
            float v1 = fontTexture.getV1(code);

            float x0 = x;
            float y0 = y;
            float x1 = x + charWidth;
            float y1 = y + charHeight;

            // position + uv
            addVertex(vertices, x0, y0, u0, v1);
            addVertex(vertices, x1, y0, u1, v1);
            addVertex(vertices, x1, y1, u1, v0);
            addVertex(vertices, x0, y1, u0, v0);

            indices.add(vertexIndex);
            indices.add(vertexIndex + 1);
            indices.add(vertexIndex + 2);
            indices.add(vertexIndex + 2);
            indices.add(vertexIndex + 3);
            indices.add(vertexIndex);

            vertexIndex += 4;
            x += charWidth;
        }

        int vao = glGenVertexArrays();
        int vbo = glGenBuffers();
        int ebo = glGenBuffers();

        glBindVertexArray(vao);

        FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(vertices.size());
        for (Float f : vertices) {
            vertexBuffer.put(f);
        }
        vertexBuffer.flip();

        IntBuffer indexBuffer = MemoryUtil.memAllocInt(indices.size());
        for (Integer i : indices) {
            indexBuffer.put(i);
        }
        indexBuffer.flip();

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_DYNAMIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_DYNAMIC_DRAW);

        int stride = 4 * Float.BYTES;

        glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, 2L * Float.BYTES);
        glEnableVertexAttribArray(1);

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        textShader.bind();
        textShader.setUniform("screenSize", (float) screenWidth, (float) screenHeight);
        textShader.setUniform("textColor", 1.0f, 1.0f, 1.0f);
        textShader.setUniform("fontTexture", 0);

        fontTexture.getTexture().bind();

        glDrawElements(GL_TRIANGLES, indices.size(), GL_UNSIGNED_INT, 0);

        textShader.unbind();

        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);

        glBindVertexArray(0);

        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
        glDeleteVertexArrays(vao);

        MemoryUtil.memFree(vertexBuffer);
        MemoryUtil.memFree(indexBuffer);
    }

    private void addVertex(List<Float> vertices, float x, float y, float u, float v) {
        vertices.add(x);
        vertices.add(y);
        vertices.add(u);
        vertices.add(v);
    }

    public void cleanup() {
        if (textShader != null) {
            textShader.cleanup();
        }
        if (fontTexture != null) {
            fontTexture.cleanup();
        }
    }
}