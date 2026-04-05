package com.odin.odinengine.render;

public class FontTexture {

    private final Texture texture;
    private final int columns;
    private final int rows;

    public FontTexture(String texturePath, int columns, int rows) {
        this.texture = new Texture(texturePath);
        this.columns = columns;
        this.rows = rows;
    }

    public Texture getTexture() {
        return texture;
    }

    public float getU0(int characterCode) {
        int column = characterCode % columns;
        return (float) column / columns;
    }

    public float getV0(int characterCode) {
        int row = characterCode / columns;
        return (float) row / rows;
    }

    public float getU1(int characterCode) {
        int column = characterCode % columns;
        return (float) (column + 1) / columns;
    }

    public float getV1(int characterCode) {
        int row = characterCode / columns;
        return (float) (row + 1) / rows;
    }

    public void cleanup() {
        texture.cleanup();
    }
}