package com.game.renderer.shader;

import com.game.math.Vec2f;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glDetachShader;

public class ShaderProgram {
    private final int id;

    private ShaderProgram(Shader vertexShader, Shader fragmentShader, Shader geometryShader) {
        id = glCreateProgram();
        glAttachShader(id, vertexShader.id());
        glAttachShader(id, fragmentShader.id());
        if (geometryShader != null)
            glAttachShader(id, geometryShader.id());
        glLinkProgram(id);
        if (glGetProgrami(id, GL_LINK_STATUS) == GL_FALSE) {
            throw new IllegalStateException("Couldn't link the shader program");
        }

        glDetachShader(id, vertexShader.id());
        glDetachShader(id, fragmentShader.id());
        if (geometryShader != null)
            glDetachShader(id, geometryShader.id());
        vertexShader.delete();
        fragmentShader.delete();
        if (geometryShader != null)
            geometryShader.delete();
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

    public void setFloat(String name, float value) {
        int location = glGetUniformLocation(id, name);
        if (location == -1)
            throw new RuntimeException("Couldn't locate uniform with name " + name);

        glUniform1f(location, value);
    }

    public void setVec2f(String name, Vec2f value) {
        int location = glGetUniformLocation(id, name);
        if (location == -1)
            throw new RuntimeException("Couldn't locate uniform with name " + name);

        glUniform2f(location, value.x(), value.y());
    }

    public void delete() {
        glDeleteProgram(id);
    }

    public static ShaderProgram fromSources(String vertexSource, String fragmentSource) {
        return new ShaderProgram(
                Shader.fromSource(ShaderType.VERTEX, vertexSource),
                Shader.fromSource(ShaderType.FRAGMENT, fragmentSource),
                null
        );
    }

    public static ShaderProgram fromPaths(Path vertexPath, Path fragmentPath) {
        return new ShaderProgram(
                Shader.fromPath(ShaderType.VERTEX, vertexPath),
                Shader.fromPath(ShaderType.FRAGMENT, fragmentPath),
                null
        );
    }

    public static ShaderProgram fromSources(String vertexSource, String fragmentSource, String geometrySource) {
        return new ShaderProgram(
                Shader.fromSource(ShaderType.VERTEX, vertexSource),
                Shader.fromSource(ShaderType.FRAGMENT, fragmentSource),
                Shader.fromSource(ShaderType.GEOMETRY, geometrySource)

        );
    }

    public static ShaderProgram fromPaths(Path vertexPath, Path fragmentPath, Path geometryPath) {
        return new ShaderProgram(
                Shader.fromPath(ShaderType.VERTEX, vertexPath),
                Shader.fromPath(ShaderType.FRAGMENT, fragmentPath),
                Shader.fromPath(ShaderType.GEOMETRY, geometryPath)
        );
    }
}
