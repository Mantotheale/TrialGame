package com.game.renderer.shader;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glShaderSource;

class Shader {
    private final int id;
    private final ShaderType type;

    private Shader(ShaderType shaderType, String shaderSource) {
        this.id = glCreateShader(shaderType.glValue());
        this.type = shaderType;

        glShaderSource(id, shaderSource);
        glCompileShader(id);
        if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new IllegalStateException("Couldn't compile the shader.\nShaderType: " + shaderType + "\nSource:\n" + shaderSource);
        }
    }

    public int id() {
        return id;
    }

    public ShaderType type() {
        return type;
    }

    public void delete() {
        glDeleteShader(id);
    }

    public static Shader fromSource(ShaderType shaderType, String source) {
        return new Shader(shaderType, source);
    }

    public static Shader fromPath(ShaderType shaderType, Path sourcePath) {
        String source;
        try {
            source = Files.readString(sourcePath);
        } catch (Exception e) {
            throw new RuntimeException("Couldn't read shader source from: " + sourcePath, e);
        }

        return new Shader(shaderType, source);
    }
}
