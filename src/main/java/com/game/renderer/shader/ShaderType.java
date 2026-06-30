package com.game.renderer.shader;

import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;

enum ShaderType {
    VERTEX(GL_VERTEX_SHADER),
    FRAGMENT(GL_FRAGMENT_SHADER),
    GEOMETRY(GL_GEOMETRY_SHADER);

    private final int glValue;

    ShaderType(int glValue) {
        this.glValue = glValue;
    }

    public int glValue() {
        return glValue;
    }
}
