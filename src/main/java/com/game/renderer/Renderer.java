package com.game.renderer;

import com.game.camera.Camera;
import com.game.event.bus.EventBus;
import com.game.event.bus.EventObserver;
import com.game.event.deferred.FrameBufferResizedEvent;
import com.game.math.Circle;
import com.game.math.Rectangle;
import com.game.math.Segment;
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
    private final ShaderProgram solidColorShaderProgram;
    private final ShaderProgram lineShaderProgram;
    private final ShaderProgram circleShaderProgram;
    private Camera camera;
    private Vec2f gridCenter;
    private final QuadBuffer gridBuffer;
    private final QuadBuffer squareBuffer;
    private final ByteBuffer tempQuadBuffer;
    private int insertedSquares;
    private final LineBuffer lineBuffer;
    private final ByteBuffer tempLineBuffer;
    private int insertedLines;
    private Vec2f viewportSize;
    private final CircleBuffer circleBuffer;
    private final ByteBuffer tempCircleBuffer;
    private int insertedCircles;

    private final EventObserver<FrameBufferResizedEvent> onFrameBufferResizedFunc = this::onFrameBufferResized;

    public Renderer(EventBus bus, Vec2f viewportSize) {
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

        VertexLayout squareVertexLayout = new VertexLayout.Builder()
                .pushFloats(2)
                .pushFloats(4)
                .build();
        this.squareBuffer = new QuadBuffer(squareVertexLayout, 10);
        this.tempQuadBuffer = MemoryUtil.memAlloc(10 * 4 * squareVertexLayout.size());
        this.insertedSquares = 0;

        VertexLayout lineVertexLayout = new VertexLayout.Builder()
                .pushFloats(2)
                .pushFloats(4)
                .pushFloats(1)
                .build();
        this.lineBuffer = new LineBuffer(lineVertexLayout, 10);
        this.tempLineBuffer = MemoryUtil.memAlloc(10 * 2 * lineVertexLayout.size());
        this.insertedLines = 0;

        this.circleBuffer = new CircleBuffer(10);
        this.tempCircleBuffer = MemoryUtil.memAlloc(10 * (2 + 1 + 4) * Float.BYTES);
        this.insertedCircles = 0;

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

        solidColorShaderProgram = ShaderProgram.fromPaths(
                Path.of("src/main/resources/shaders/solid_color_vertex_shader.vert"),
                Path.of("src/main/resources/shaders/solid_color_fragment_shader.frag")
        );

        lineShaderProgram = ShaderProgram.fromPaths(
                Path.of("src/main/resources/shaders/line_vertex_shader.vert"),
                Path.of("src/main/resources/shaders/line_fragment_shader.frag"),
                Path.of("src/main/resources/shaders/line_geometry_shader.geom")
        );

        circleShaderProgram = ShaderProgram.fromPaths(
                Path.of("src/main/resources/shaders/circle_vertex_shader.vert"),
                Path.of("src/main/resources/shaders/circle_fragment_shader.frag"),
                Path.of("src/main/resources/shaders/circle_geometry_shader.geom")
        );

        commandQueue = new PriorityQueue<>();

        bus.addObserver(FrameBufferResizedEvent.class, onFrameBufferResizedFunc);
        this.viewportSize = viewportSize;
    }

    public void beginScene(Camera camera) {
        this.camera = camera;
    }

    public void submit(Transform2D transform, Texture texture) {
        if (commandQueue.size() >= MAX_QUAD_COUNT) throw new IllegalStateException("Too many pushed quads, max is " + MAX_QUAD_COUNT);
        commandQueue.add(new RenderCommand(transform, texture));
    }

    public void addGrid(Vec2f center) {
        this.gridCenter = center;
    }

    public void addRect(Rectangle rect, float r, float g, float b, float a) {
        Vec2f bottomLeft = new Vec2f(rect.left(), rect.bottom());
        Vec2f bottomRight = new Vec2f(rect.right(), rect.bottom());
        Vec2f topRight = new Vec2f(rect.right(), rect.top());
        Vec2f topLeft = new Vec2f(rect.left(), rect.top());

        tempQuadBuffer.putFloat(bottomLeft.x()).putFloat(bottomLeft.y()).putFloat(r).putFloat(g).putFloat(b).putFloat(a);
        tempQuadBuffer.putFloat(bottomRight.x()).putFloat(bottomRight.y()).putFloat(r).putFloat(g).putFloat(b).putFloat(a);
        tempQuadBuffer.putFloat(topRight.x()).putFloat(topRight.y()).putFloat(r).putFloat(g).putFloat(b).putFloat(a);
        tempQuadBuffer.putFloat(topLeft.x()).putFloat(topLeft.y()).putFloat(r).putFloat(g).putFloat(b).putFloat(a);

        insertedSquares++;
    }

    public void addSegment(Segment segment, float thickness, float r, float g, float b, float a) {
        tempLineBuffer
                .putFloat(segment.origin().x()).putFloat(segment.origin().y())
                .putFloat(r).putFloat(g).putFloat(b).putFloat(a)
                .putFloat(thickness);
        tempLineBuffer
                .putFloat(segment.destination().x()).putFloat(segment.destination().y())
                .putFloat(r).putFloat(g).putFloat(b).putFloat(a)
                .putFloat(thickness);
        insertedLines++;
    }

    public void addCircle(Circle circle, float r, float g, float b, float a) {
        tempCircleBuffer
                .putFloat(circle.center().x()).putFloat(circle.center().y())
                .putFloat(circle.radius())
                .putFloat(r).putFloat(g).putFloat(b).putFloat(a);
        insertedCircles++;
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

        if (insertedSquares > 0) {
            squareBuffer.setData(tempQuadBuffer.flip());
            glUseProgram(solidColorShaderProgram.id());
            solidColorShaderProgram.setMatrix4f("viewProjection", camera.matrix());
            glBindVertexArray(squareBuffer.id());
            glDrawElements(GL_TRIANGLES, QuadBuffer.INDICES_PER_QUAD * insertedSquares, GL_UNSIGNED_INT, 0);

            tempQuadBuffer.clear();
            insertedSquares = 0;
        }

        if (insertedLines > 0) {
            lineBuffer.setData(tempLineBuffer.flip());
            glUseProgram(lineShaderProgram.id());
            lineShaderProgram.setMatrix4f("viewProjection", camera.matrix());
            lineShaderProgram.setVec2f("viewportSize", viewportSize);
            glBindVertexArray(lineBuffer.id());
            glDrawArrays(GL_LINES, 0, insertedLines * 2);

            tempLineBuffer.clear();
            insertedLines = 0;
        }

        if (insertedCircles > 0) {
            circleBuffer.setData(tempCircleBuffer.flip());
            glUseProgram(circleShaderProgram.id());
            circleShaderProgram.setMatrix4f("viewProjection", camera.matrix());
            glBindVertexArray(circleBuffer.id());
            glDrawArrays(GL_POINTS, 0, insertedCircles);

            tempCircleBuffer.clear();
            insertedCircles = 0;
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
        viewportSize = new Vec2f(event.newWidth(), event.newHeight());
    }

    private final static Vec2f BOTTOM_LEFT = new Vec2f(-0.5f, -0.5f).scale(20);
    private final static Vec2f BOTTOM_RIGHT = new Vec2f(0.5f, -0.5f).scale(20);
    private final static Vec2f TOP_RIGHT = new Vec2f(0.5f, 0.5f).scale(20);
    private final static Vec2f TOP_LEFT = new Vec2f(-0.5f, 0.5f).scale(20);

}
