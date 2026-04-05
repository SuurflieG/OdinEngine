package com.odin.odinengine.render;

import org.lwjgl.BufferUtils;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryUtil.memFree;

public class Texture {

    private final int id;
    private final int width;
    private final int height;

    public Texture(String resourcePath) {
        ByteBuffer imageBuffer = loadResource(resourcePath);

        IntBuffer widthBuffer = BufferUtils.createIntBuffer(1);
        IntBuffer heightBuffer = BufferUtils.createIntBuffer(1);
        IntBuffer channelsBuffer = BufferUtils.createIntBuffer(1);

        stbi_set_flip_vertically_on_load(false);
        ByteBuffer pixels = stbi_load_from_memory(imageBuffer, widthBuffer, heightBuffer, channelsBuffer, 4);
        if (pixels == null) {
            throw new RuntimeException("Failed to load texture: " + resourcePath + " | " + stbi_failure_reason());
        }

        width = widthBuffer.get(0);
        height = heightBuffer.get(0);

        id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
        glGenerateMipmap(GL_TEXTURE_2D);

        stbi_image_free(pixels);
        memFree(imageBuffer);
    }

    private ByteBuffer loadResource(String resourcePath) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new RuntimeException("Texture resource not found: " + resourcePath);
            }

            byte[] bytes = inputStream.readAllBytes();
            ByteBuffer buffer = org.lwjgl.system.MemoryUtil.memAlloc(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            return buffer;
        } catch (Exception e) {
            throw new RuntimeException("Failed to read texture resource: " + resourcePath, e);
        }
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }

    public void cleanup() {
        glDeleteTextures(id);
    }

    public int getId() {
        return id;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}