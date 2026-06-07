package com.game.renderer.texture;

import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER;
import static org.lwjgl.opengl.GL14.GL_MIRRORED_REPEAT;

public enum TextureWrap {
    CLAMP_TO_EDGE(GL_CLAMP_TO_EDGE),
    CLAMP_TO_BORDER(GL_CLAMP_TO_BORDER),
    MIRRORED_REPEAT(GL_MIRRORED_REPEAT),
    REPEAT(GL_REPEAT);

    TextureWrap(int glValue) {
        this.glValue = glValue;
    }

    private final int glValue;

    public int glValue() {
        return glValue;
    }
}
