package com.game.renderer.shader;

import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glDetachShader;

public class ShaderProgram {
    private final int id;

    private ShaderProgram(Shader vertexShader, Shader fragmentShader) {
        if (vertexShader.type() != ShaderType.VERTEX)
            throw new IllegalArgumentException("The specified shader is not a vertex shader");

        if (fragmentShader.type() != ShaderType.FRAGMENT)
            throw new IllegalArgumentException("The specified shader is not a fragment shader");

        id = glCreateProgram();
        glAttachShader(id, vertexShader.id());
        glAttachShader(id, fragmentShader.id());
        glLinkProgram(id);
        if (glGetProgrami(id, GL_LINK_STATUS) == GL_FALSE) {
            throw new IllegalStateException("Couldn't link the shader program");
        }

        glDetachShader(id, vertexShader.id());
        glDetachShader(id, fragmentShader.id());
        vertexShader.delete();
        fragmentShader.delete();
    }

    public int id() {
        return id;
    }

    public void setMatrix4f(String name, Matrix4fc value) {
        int location = glGetUniformLocation(id, name);
        if (location == -1)
            throw new RuntimeException("Couldn't locate uniform with name " + name);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer =  value.get(stack.mallocFloat(16));
            glUniformMatrix4fv(location, false, buffer);
        }
    }

    public void setInt(String name, int value) {
        int location = glGetUniformLocation(id, name);
        if (location == -1)
            throw new RuntimeException("Couldn't locate uniform with name " + name);

        glUniform1i(location, value);
    }

    public void delete() {
        glDeleteProgram(id);
    }

    public static ShaderProgram fromSources(String vertexSource, String fragmentSource) {
        return new ShaderProgram(
                Shader.fromSource(ShaderType.VERTEX, vertexSource),
                Shader.fromSource(ShaderType.FRAGMENT, fragmentSource)
        );
    }

    public static ShaderProgram fromPaths(Path vertexPath, Path fragmentPath) {
        return new ShaderProgram(
                Shader.fromPath(ShaderType.VERTEX, vertexPath),
                Shader.fromPath(ShaderType.FRAGMENT, fragmentPath)
        );
    }
}
