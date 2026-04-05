package com.odin.odinengine.render;

public class HUDRenderer {

    private Shader hudShader;
    private int vaoId;
    private int vboId;

    public void init() {
        hudShader = new Shader(
                "src/main/resources/shaders/hud_vertex.glsl",
                "src/main/resources/shaders/hud_fragment.glsl"
        );

        float size = 0.015f;
        float[] vertices = {
                -size,  0.0f,
                size,  0.0f,

                0.0f, -size,
                0.0f,  size
        };

        vaoId = org.lwjgl.opengl.GL30.glGenVertexArrays();
        vboId = org.lwjgl.opengl.GL15.glGenBuffers();

        org.lwjgl.opengl.GL30.glBindVertexArray(vaoId);

        java.nio.FloatBuffer vertexBuffer = org.lwjgl.system.MemoryUtil.memAllocFloat(vertices.length);
        vertexBuffer.put(vertices).flip();

        org.lwjgl.opengl.GL15.glBindBuffer(org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER, vboId);
        org.lwjgl.opengl.GL15.glBufferData(org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER, vertexBuffer, org.lwjgl.opengl.GL15.GL_STATIC_DRAW);

        org.lwjgl.opengl.GL20.glVertexAttribPointer(0, 2, org.lwjgl.opengl.GL11.GL_FLOAT, false, 2 * Float.BYTES, 0);
        org.lwjgl.opengl.GL20.glEnableVertexAttribArray(0);

        org.lwjgl.opengl.GL15.glBindBuffer(org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER, 0);
        org.lwjgl.opengl.GL30.glBindVertexArray(0);

        org.lwjgl.system.MemoryUtil.memFree(vertexBuffer);
    }

    public void render(int screenWidth, int screenHeight, String targetedBlockName) {
        org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_DEPTH_TEST);

        hudShader.bind();
        hudShader.setUniform("hudColor", 1.0f, 1.0f, 1.0f);

        org.lwjgl.opengl.GL30.glBindVertexArray(vaoId);
        org.lwjgl.opengl.GL11.glDrawArrays(org.lwjgl.opengl.GL11.GL_LINES, 0, 4);
        org.lwjgl.opengl.GL30.glBindVertexArray(0);

        hudShader.unbind();

        org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_DEPTH_TEST);
    }

    public void cleanup() {
        if (hudShader != null) {
            hudShader.cleanup();
        }

        org.lwjgl.opengl.GL15.glDeleteBuffers(vboId);
        org.lwjgl.opengl.GL30.glDeleteVertexArrays(vaoId);
    }
}