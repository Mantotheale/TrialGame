package com.game.renderer.texture;

import static org.lwjgl.opengl.GL11.*;

public enum TextureMinifyingFilter {
    LINEAR(GL_LINEAR),
    NEAREST(GL_NEAREST),
    NEAREST_MIPMAP_NEAREST(GL_NEAREST_MIPMAP_NEAREST),
    LINEAR_MIPMAP_NEAREST(GL_LINEAR_MIPMAP_NEAREST),
    NEAREST_MIPMAP_LINEAR(GL_NEAREST_MIPMAP_LINEAR),
    LINEAR_MIPMAP_LINEAR(GL_LINEAR_MIPMAP_LINEAR);

    TextureMinifyingFilter(int glValue) {
        this.glValue = glValue;
    }

    private final int glValue;

    public int glValue() {
        return glValue;
    }
}
