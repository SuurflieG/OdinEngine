package com.odin.odinengine.render;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBTruetype.stbtt_GetBakedQuad;

public class TTFTextRenderer {
    private Shader shader;
    private TTFFont font;

    private int vaoId;
    private int vboId;
    private int eboId;

    public void init(String fontPath, float pixelHeight) {
        shader = new Shader(
                "src/main/resources/shaders/ttf_text_vertex.glsl",
                "src/main/resources/shaders/ttf_text_fragment.glsl"
        );

        font = new TTFFont(fontPath, pixelHeight);
        font.init();

        vaoId = glGenVertexArrays();
        vboId = glGenBuffers();
        eboId = glGenBuffers();

        glBindVertexArray(vaoId);

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, 0, GL_DYNAMIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, 0, GL_DYNAMIC_DRAW);

        // layout(location = 0) -> vec2 position
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0L);
        glEnableVertexAttribArray(0);

        // layout(location = 1) -> vec2 texCoord
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2L * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void renderText(String text, float x, float y, int screenWidth, int screenHeight) {
        if (text == null || text.isBlank()) {
            return;
        }

        int maxGlyphs = text.length();
        float[] vertexArray = new float[maxGlyphs * 4 * 4];
        int[] indexArray = new int[maxGlyphs * 6];

        int vertexOffset = 0;
        int indexOffset = 0;
        int quadIndex = 0;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer xBuf = stack.floats(x);
            FloatBuffer yBuf = stack.floats(y);
            STBTTAlignedQuad quad = STBTTAlignedQuad.malloc(stack);

            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);

                if (c == '\n') {
                    xBuf.put(0, x);
                    yBuf.put(0, yBuf.get(0) + 24.0f);
                    continue;
                }

                if (c < font.getFirstChar() || c >= font.getFirstChar() + font.getCharCount()) {
                    c = '?';
                    if (c < font.getFirstChar() || c >= font.getFirstChar() + font.getCharCount()) {
                        continue;
                    }
                }

                stbtt_GetBakedQuad(
                        font.getBakedChars(),
                        font.getBitmapWidth(),
                        font.getBitmapHeight(),
                        c - font.getFirstChar(),
                        xBuf,
                        yBuf,
                        quad,
                        true
                );

                float x0 = quad.x0();
                float y0 = quad.y0();
                float x1 = quad.x1();
                float y1 = quad.y1();

                float s0 = quad.s0();
                float t0 = quad.t0();
                float s1 = quad.s1();
                float t1 = quad.t1();

                // top-left
                vertexArray[vertexOffset++] = x0;
                vertexArray[vertexOffset++] = y0;
                vertexArray[vertexOffset++] = s0;
                vertexArray[vertexOffset++] = t0;

                // top-right
                vertexArray[vertexOffset++] = x1;
                vertexArray[vertexOffset++] = y0;
                vertexArray[vertexOffset++] = s1;
                vertexArray[vertexOffset++] = t0;

                // bottom-right
                vertexArray[vertexOffset++] = x1;
                vertexArray[vertexOffset++] = y1;
                vertexArray[vertexOffset++] = s1;
                vertexArray[vertexOffset++] = t1;

                // bottom-left
                vertexArray[vertexOffset++] = x0;
                vertexArray[vertexOffset++] = y1;
                vertexArray[vertexOffset++] = s0;
                vertexArray[vertexOffset++] = t1;

                int baseVertex = quadIndex * 4;
                indexArray[indexOffset++] = baseVertex;
                indexArray[indexOffset++] = baseVertex + 1;
                indexArray[indexOffset++] = baseVertex + 2;
                indexArray[indexOffset++] = baseVertex + 2;
                indexArray[indexOffset++] = baseVertex + 3;
                indexArray[indexOffset++] = baseVertex;

                quadIndex++;
            }
        }

        if (quadIndex == 0) {
            return;
        }

        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(quadIndex * 4 * 4);
        vertexBuffer.put(vertexArray, 0, quadIndex * 4 * 4).flip();

        IntBuffer indexBuffer = BufferUtils.createIntBuffer(quadIndex * 6);
        indexBuffer.put(indexArray, 0, quadIndex * 6).flip();

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        shader.bind();
        shader.setUniform("screenSize", (float) screenWidth, (float) screenHeight);
        shader.setUniform("textColor", 1.0f, 1.0f, 1.0f);
        shader.setUniform("fontSampler", 0);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, font.getTextureId());

        glBindVertexArray(vaoId);

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_DYNAMIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_DYNAMIC_DRAW);

        glDrawElements(GL_TRIANGLES, quadIndex * 6, GL_UNSIGNED_INT, 0L);

        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);
        shader.unbind();

        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    public void cleanup() {
        if (shader != null) {
            shader.cleanup();
            shader = null;
        }

        if (font != null) {
            font.cleanup();
            font = null;
        }

        if (eboId != 0) {
            glDeleteBuffers(eboId);
            eboId = 0;
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