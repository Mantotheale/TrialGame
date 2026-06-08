package com.game.renderer;

import com.game.camera.Camera;
import com.game.input.Input;
import com.game.input.ResizeFrameBuffer;
import com.game.renderer.shader.ShaderProgram;
import com.game.renderer.texture.Texture;
import com.game.transform.Transform;
import com.game.util.Observer;
import com.game.window.Window;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class Renderer implements Observer<Input> {
    private static final int MAX_QUAD_COUNT = 10;
    private static final int MAX_TEXTURE_COUNT = 16;

    private final QuadArrayBuffer quadArrayBuffer;
    private final ShaderProgram shaderProgram;
    private final List<RenderCommand> pushedCommands;
    private Camera camera;

    public Renderer(Window window) {
        pushedCommands = new ArrayList<>();

        float[] vertices = {
                -0.5f, -0.5f, 0, 0, 0,
                0.5f, -0.5f, 0, 1, 0,
                0.5f, 0.5f, 0, 1, 1,
                -0.5f, 0.5f, 0, 0, 1
        };

        quadArrayBuffer = new QuadArrayBuffer(1, 5 * Float.BYTES);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer verticesBuffer = stack.mallocFloat(vertices.length);
            verticesBuffer.put(vertices).flip();
            quadArrayBuffer.setData(verticesBuffer);
        }

        shaderProgram = ShaderProgram.fromPaths(
                Path.of("src/main/resources/shaders/vertexshader.vert"),
                Path.of("src/main/resources/shaders/fragmentshader.frag")
        );

        window.addObserver(this);
    }

    public void beginScene(Camera camera) {
        this.camera = camera;
    }

    public void submit(Transform transform, Texture texture) {
        if (pushedCommands.size() >= MAX_QUAD_COUNT)
            throw new IllegalStateException("Too many pushed commands");

        pushedCommands.add(new RenderCommand(transform, texture));
    }

    public void endScene() {
        glClear(GL_COLOR_BUFFER_BIT);

        glUseProgram(shaderProgram.id());

        shaderProgram.setMatrix4f("viewProjection", camera.matrix());

        for (RenderCommand command : pushedCommands) {
            glBindVertexArray(quadArrayBuffer.id());
            glBindTexture(GL_TEXTURE_2D, command.texture.id());

            shaderProgram.setMatrix4f("model", command.transform.matrix());

            glDrawElements(GL_TRIANGLES, QuadArrayBuffer.INDICES_PER_QUAD, GL_UNSIGNED_INT, 0);
        }

        pushedCommands.clear();
        camera = null;
    }

    public void delete() {
        quadArrayBuffer.delete();
        shaderProgram.delete();
    }

    public void setViewport(int originX, int originY, int width, int height) {
        glViewport(originX, originY, width, height);
    }

    public void setClearColor(float r, float g, float b, float a) {
        glClearColor(r, g, b, a);
    }

    @Override
    public void handle(Input value) {
        if (value instanceof ResizeFrameBuffer(int width, int height))
            setViewport(0, 0, width, height);
    }

    private record RenderCommand(Transform transform, Texture texture) { }
}
