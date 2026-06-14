package com.game.renderer;

import com.game.camera.Camera;
import com.game.event.Event;
import com.game.event.EventObserver;
import com.game.event.FrameBufferResizedEvent;
import com.game.renderer.shader.ShaderProgram;
import com.game.renderer.texture.Texture;
import com.game.transform.Transform2D;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.PriorityQueue;

import static org.lwjgl.opengl.GL20.*;

public class Renderer implements EventObserver {
    private static final int MAX_QUAD_COUNT = 10000;
    private static final int TEXTURE_UNITS = 8;

    private final PriorityQueue<RenderCommand> commandQueue;
    private final Batch batch;
    private final ShaderProgram shaderProgram;
    private Camera camera;

    public Renderer() {
        VertexLayout vertexLayout = new VertexLayout.Builder()
                .pushFloats(2)
                .pushInts(1)
                .pushFloats(2)
                .build();
        QuadBuffer quadBuffer = new QuadBuffer(vertexLayout, MAX_QUAD_COUNT);
        ByteBuffer intermediateBuffer = MemoryUtil.memAlloc(MAX_QUAD_COUNT * QuadBuffer.VERTICES_PER_QUAD * vertexLayout.size());
        this.batch = new Batch(quadBuffer, intermediateBuffer, TEXTURE_UNITS);

        shaderProgram = ShaderProgram.fromPaths(
                Path.of("src/main/resources/shaders/vertexshader.vert"),
                Path.of("src/main/resources/shaders/fragmentshader.frag")
        );
        glUseProgram(shaderProgram.id());
        for (int i = 0; i < TEXTURE_UNITS; i++)
            shaderProgram.setInt("tex[" + i + "]", i);

        commandQueue = new PriorityQueue<>();
    }

    public void beginScene(Camera camera) {
        this.camera = camera;
    }

    public void submit(Transform2D transform, Texture texture) {
        if (commandQueue.size() >= MAX_QUAD_COUNT) throw new IllegalStateException("Too many pushed quads, max is " + MAX_QUAD_COUNT);
        commandQueue.add(new RenderCommand(transform, texture));
    }

    public void endScene() {
        glClear(GL_COLOR_BUFFER_BIT);

        glUseProgram(shaderProgram.id());
        shaderProgram.setMatrix4f("viewProjection", camera.matrix());

        while (!commandQueue.isEmpty()) {
            RenderCommand command = commandQueue.poll();
            if (!batch.canAddCommand(command))
                batch.flush();
            batch.addCommand(command);
        }

        batch.flush();
        camera = null;
    }

    public void delete() {
        batch.delete();
        shaderProgram.delete();
        commandQueue.clear();
    }

    public void setViewport(int originX, int originY, int width, int height) {
        glViewport(originX, originY, width, height);
    }

    public void setClearColor(float r, float g, float b, float a) {
        glClearColor(r, g, b, a);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof FrameBufferResizedEvent(int newWidth, int newHeight))
            setViewport(0, 0, newWidth, newHeight);
    }
}
