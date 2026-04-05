package com.odin.odinengine.world;

public class BlockDefinitionFile {

    private String name;
    private boolean solid;
    private Textures textures;

    public String getName() {
        return name;
    }

    public boolean isSolid() {
        return solid;
    }

    public Textures getTextures() {
        return textures;
    }

    public static class Textures {
        private String all;
        private String top;
        private String bottom;
        private String side;

        public String getAll() {
            return all;
        }

        public String getTop() {
            return top;
        }

        public String getBottom() {
            return bottom;
        }

        public String getSide() {
            return side;
        }
    }
}