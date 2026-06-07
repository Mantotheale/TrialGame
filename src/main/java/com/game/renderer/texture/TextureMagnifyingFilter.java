package com.game.renderer.texture;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;

public enum TextureMagnifyingFilter {
    LINEAR(GL_LINEAR),
    NEAREST(GL_NEAREST);

    TextureMagnifyingFilter(int glValue) {
        this.glValue = glValue;
    }

    private final int glValue;

    public int glValue() {
        return glValue;
    }
}
