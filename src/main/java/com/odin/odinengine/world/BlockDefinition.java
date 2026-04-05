package com.odin.odinengine.world;

public class BlockDefinition {

    private final short id;
    private final String name;
    private final boolean solid;

    private final String allTexturePath;
    private final String topTexturePath;
    private final String bottomTexturePath;
    private final String sideTexturePath;

    public BlockDefinition(short id,
                           String name,
                           boolean solid,
                           String allTexturePath,
                           String topTexturePath,
                           String bottomTexturePath,
                           String sideTexturePath) {
        this.id = id;
        this.name = name;
        this.solid = solid;
        this.allTexturePath = allTexturePath;
        this.topTexturePath = topTexturePath;
        this.bottomTexturePath = bottomTexturePath;
        this.sideTexturePath = sideTexturePath;
    }

    public short getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isSolid() {
        return solid;
    }

    public String getAllTexturePath() {
        return allTexturePath;
    }

    public String getTopTexturePath() {
        return topTexturePath;
    }

    public String getBottomTexturePath() {
        return bottomTexturePath;
    }

    public String getSideTexturePath() {
        return sideTexturePath;
    }

    public String getTexturePath(Direction direction) {
        if (allTexturePath != null && !allTexturePath.isBlank()) {
            return allTexturePath;
        }

        return switch (direction) {
            case TOP -> topTexturePath != null ? topTexturePath : sideTexturePath;
            case BOTTOM -> bottomTexturePath != null ? bottomTexturePath : sideTexturePath;
            case FRONT, BACK, LEFT, RIGHT -> sideTexturePath;
        };
    }
}