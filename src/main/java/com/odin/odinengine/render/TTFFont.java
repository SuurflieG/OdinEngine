package com.odin.odinengine.render;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.stb.STBTruetype.stbtt_BakeFontBitmap;

public class TTFFont {
    private static final int FIRST_CHAR = 32;
    private static final int CHAR_COUNT = 95; // ASCII 32..126
    private static final int BITMAP_WIDTH = 512;
    private static final int BITMAP_HEIGHT = 512;

    private final String fontPath;
    private final float pixelHeight;

    private int textureId;
    private STBTTBakedChar.Buffer bakedChars;

    public TTFFont(String fontPath, float pixelHeight) {
        this.fontPath = fontPath;
        this.pixelHeight = pixelHeight;
    }

    public void init() {
        ByteBuffer ttfBuffer = null;
        ByteBuffer bitmap = null;

        try {
            byte[] fontBytes = Files.readAllBytes(Path.of(fontPath));
            ttfBuffer = BufferUtils.createByteBuffer(fontBytes.length);
            ttfBuffer.put(fontBytes);
            ttfBuffer.flip();

            bitmap = BufferUtils.createByteBuffer(BITMAP_WIDTH * BITMAP_HEIGHT);
            bakedChars = STBTTBakedChar.malloc(CHAR_COUNT);

            int result = stbtt_BakeFontBitmap(
                    ttfBuffer,
                    pixelHeight,
                    bitmap,
                    BITMAP_WIDTH,
                    BITMAP_HEIGHT,
                    FIRST_CHAR,
                    bakedChars
            );

            if (result <= 0) {
                throw new RuntimeException("Failed to bake TTF font bitmap: " + fontPath);
            }

            textureId = glGenTextures();
            System.out.println("TTF baked successfully: " + fontPath + ", textureId=" + textureId);
            glBindTexture(GL_TEXTURE_2D, textureId);

            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            glTexImage2D(
                    GL_TEXTURE_2D,
                    0,
                    GL_RED,
                    BITMAP_WIDTH,
                    BITMAP_HEIGHT,
                    0,
                    GL_RED,
                    GL_UNSIGNED_BYTE,
                    bitmap
            );

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

            glBindTexture(GL_TEXTURE_2D, 0);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load TTF font: " + fontPath, e);
        } finally {
            if (bitmap != null) {
                // BufferUtils buffer, no manual free
            }
            if (ttfBuffer != null && MemoryUtil.memAddressSafe(ttfBuffer) != MemoryUtil.NULL) {
                // BufferUtils buffer, no manual free
            }
        }
    }

    public int getTextureId() {
        return textureId;
    }

    public STBTTBakedChar.Buffer getBakedChars() {
        return bakedChars;
    }

    public int getFirstChar() {
        return FIRST_CHAR;
    }

    public int getCharCount() {
        return CHAR_COUNT;
    }

    public int getBitmapWidth() {
        return BITMAP_WIDTH;
    }

    public int getBitmapHeight() {
        return BITMAP_HEIGHT;
    }

    public void cleanup() {
        if (textureId != 0) {
            glDeleteTextures(textureId);
            textureId = 0;
        }

        if (bakedChars != null) {
            bakedChars.free();
            bakedChars = null;
        }
    }
}