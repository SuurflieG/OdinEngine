package com.odin.odinengine.render;

import org.joml.Matrix4f;

public class DebugPointRenderer {
    private final DebugBlockOutlineRenderer outlineRenderer = new DebugBlockOutlineRenderer();

    public void init() {
        outlineRenderer.init();
    }

    public void renderPoint(
            float x,
            float y,
            float z,
            Matrix4f projection,
            Matrix4f view
    ) {
        float size = 0.08f;
        outlineRenderer.renderBox(
                x - size, y - size, z - size,
                x + size, y + size, z + size,
                projection, view,
                0.0f, 1.0f, 1.0f,
                false
        );
    }

    public void cleanup() {
        outlineRenderer.cleanup();
    }
}