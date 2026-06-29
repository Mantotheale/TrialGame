package com.game.renderer;

import com.game.camera.Camera;
import com.game.event.bus.EventBus;
import com.game.event.bus.EventObserver;
import com.game.event.deferred.FrameBufferResizedEvent;
import com.game.math.Vec2f;
import com.game.renderer.shader.ShaderProgram;
import com.game.renderer.texture.Texture;
import com.game.transform.Transform2D;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.PriorityQueue;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class Renderer {
    private static final int MAX_QUAD_COUNT = 10000;
    private static final int TEXTURE_UNITS = 8;

    private final PriorityQueue<RenderCommand> commandQueue;
    private final Batch batch;
    private final ShaderProgram textureShaderProgram;
    private final ShaderProgram gridShaderProgram;
    private Camera camera;
    private Vec2f gridCenter;
    private final QuadBuffer gridBuffer;

    private final EventObserver<FrameBufferResizedEvent> onFrameBufferResizedFunc = this::onFrameBufferResized;

    public Renderer(EventBus bus) {
        VertexLayout vertexLayout = new VertexLayout.Builder()
                .pushFloats(2)
                .pushInts(1)
                .pushFloats(2)
                .build();
        QuadBuffer quadBuffer = new QuadBuffer(vertexLayout, MAX_QUAD_COUNT);
        ByteBuffer intermediateBuffer = MemoryUtil.memAlloc(MAX_QUAD_COUNT * QuadBuffer.VERTICES_PER_QUAD * vertexLayout.size());
        this.batch = new Batch(quadBuffer, intermediateBuffer, TEXTURE_UNITS);

        VertexLayout gridVertexLayout = new VertexLayout.Builder()
                .pushFloats(2)
                .build();
        this.gridBuffer = new QuadBuffer(gridVertexLayout, 1);
        this.gridCenter = null;

        textureShaderProgram = ShaderProgram.fromPaths(
                Path.of("src/main/resources/shaders/texture_vertex_shader.vert"),
                Path.of("src/main/resources/shaders/texture_fragment_shader.frag")
        );
        glUseProgram(textureShaderProgram.id());
        for (int i = 0; i < TEXTURE_UNITS; i++)
            textureShaderProgram.setInt("tex[" + i + "]", i);

        gridShaderProgram = ShaderProgram.fromPaths(
                Path.of("src/main/resources/shaders/grid_vertex_shader.vert"),
                Path.of("src/main/resources/shaders/grid_fragment_shader.frag")
        );

        commandQueue = new PriorityQueue<>();

        bus.addObserver(FrameBufferResizedEvent.class, onFrameBufferResizedFunc);
    }

    public void beginScene(Camera camera) {
        this.camera = camera;
    }

    public void submit(Transform2D transform, Texture texture) {
        if (commandQueue.size() >= MAX_QUAD_COUNT) throw new IllegalStateException("Too many pushed quads, max is " + MAX_QUAD_COUNT);
        commandQueue.add(new RenderCommand(transform, texture));
    }

    public void addGrid(Vec2f center) {
        System.out.println("PORCODIO");
        this.gridCenter = center;
    }

    public void endScene() {
        glClear(GL_COLOR_BUFFER_BIT);

        glUseProgram(textureShaderProgram.id());
        textureShaderProgram.setMatrix4f("viewProjection", camera.matrix());

        while (!commandQueue.isEmpty()) {
            RenderCommand command = commandQueue.poll();
            if (!batch.canAddCommand(command))
                batch.flush();
            batch.addCommand(command);
        }

        batch.flush();

        if (gridCenter != null) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                ByteBuffer buffer = stack.malloc(4 * 2 * Float.BYTES);
                buffer.putFloat(gridCenter.x() + BOTTOM_LEFT.x()).putFloat(gridCenter.y() + BOTTOM_LEFT.y());
                buffer.putFloat(gridCenter.x() + BOTTOM_RIGHT.x()).putFloat(gridCenter.y() + BOTTOM_RIGHT.y());
                buffer.putFloat(gridCenter.x() + TOP_RIGHT.x()).putFloat(gridCenter.y() + TOP_RIGHT.y());
                buffer.putFloat(gridCenter.x() + TOP_LEFT.x()).putFloat(gridCenter.y() + TOP_LEFT.y());
                gridBuffer.setData(buffer.flip());
            }

            glUseProgram(gridShaderProgram.id());
            gridShaderProgram.setMatrix4f("viewProjection", camera.matrix());
            glBindVertexArray(gridBuffer.id());
            glDrawElements(GL_TRIANGLES, QuadBuffer.INDICES_PER_QUAD, GL_UNSIGNED_INT, 0);

            gridCenter = null;
        }

        camera = null;
    }

    public void delete(EventBus bus) {
        batch.delete();
        textureShaderProgram.delete();
        gridShaderProgram.delete();
        commandQueue.clear();
        bus.removeObserver(FrameBufferResizedEvent.class, onFrameBufferResizedFunc);
    }

    public void setClearColor(float r, float g, float b, float a) {
        glClearColor(r, g, b, a);
    }

    private void onFrameBufferResized(EventBus bus, FrameBufferResizedEvent event) {
        glViewport(0, 0, event.newWidth(), event.newHeight());
    }

    private final static Vec2f BOTTOM_LEFT = new Vec2f(-0.5f, -0.5f);
    private final static Vec2f BOTTOM_RIGHT = new Vec2f(0.5f, -0.5f);
    private final static Vec2f TOP_RIGHT = new Vec2f(0.5f, 0.5f);
    private final static Vec2f TOP_LEFT = new Vec2f(-0.5f, 0.5f);

}
