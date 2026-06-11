package com.game.renderer;

import com.game.camera.Camera;
import com.game.input.Input;
import com.game.input.ResizeFrameBuffer;
import com.game.renderer.shader.ShaderProgram;
import com.game.renderer.texture.Texture;
import com.game.transform.Transform;
import com.game.util.Observer;
import com.game.util.Vec2f;
import com.game.window.Window;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class Renderer implements Observer<Input> {
    private static final int MAX_QUAD_COUNT = 10000;
    private static final int TEXTURE_UNITS = 16;

    private final QuadBuffer quadBuffer;
    private final ShaderProgram shaderProgram;
    private Camera camera;

    private final ByteBuffer intermediateBuffer;
    private int insertedQuads;
    private final Vector3f[] quadCorners;
    private final int[] texXMultiplier;
    private final int[] texYMultiplier;
    private final static Vector3f WORKING_MEMORY = new Vector3f();

    public Renderer(Window window) {
        VertexLayout vertexLayout = new VertexLayout.Builder()
                .pushFloats(3)
                .pushInts(1)
                .pushFloats(2)
                .build();

        this.quadBuffer = new QuadBuffer(vertexLayout, MAX_QUAD_COUNT);

        shaderProgram = ShaderProgram.fromPaths(
                Path.of("src/main/resources/shaders/vertexshader.vert"),
                Path.of("src/main/resources/shaders/fragmentshader.frag")
        );

        intermediateBuffer = MemoryUtil.memAlloc(MAX_QUAD_COUNT * QuadBuffer.VERTICES_PER_QUAD * vertexLayout.size()).order(ByteOrder.nativeOrder());
        quadCorners = new Vector3f[] {
                new Vector3f(-0.5f, -0.5f, 0),
                new Vector3f(0.5f, -0.5f, 0),
                new Vector3f(0.5f, 0.5f, 0),
                new Vector3f(-0.5f, 0.5f, 0)
        };
        texXMultiplier = new int[] { 0, 1, 1, 0};
        texYMultiplier = new int[] { 0, 0, 1, 1};
        insertedQuads = 0;

        window.addObserver(this);
    }

    public void beginScene(Camera camera) {
        this.camera = camera;
    }

    public void submit(Transform transform, Texture texture) {
        if (intermediateBuffer.position() == intermediateBuffer.limit()) throw new IllegalStateException("Too many pushed quads");

        for (int i = 0; i < 4; i++) {
            Vec2f texCorner = texture.bottomLeftCorner();
            float texWidth = texture.normalizedWidth();
            float texHeight = texture.normalizedHeight();

            Vector3f transformedCorner = transform.transform(quadCorners[i]);
            intermediateBuffer.putFloat(transformedCorner.x).putFloat(transformedCorner.y).putFloat(transformedCorner.z);
            intermediateBuffer.putInt(texture.texId());
            intermediateBuffer.putFloat(texCorner.x() + texXMultiplier[i] * texWidth);
            intermediateBuffer.putFloat(texCorner.y() + texYMultiplier[i] * texHeight);
        }

        insertedQuads++;

        glActiveTexture(GL_TEXTURE0 + texture.texId());
        glBindTexture(GL_TEXTURE_2D, texture.texId());
    }

    public void endScene() {
        glClear(GL_COLOR_BUFFER_BIT);

        glUseProgram(shaderProgram.id());
        shaderProgram.setMatrix4f("viewProjection", camera.matrix());

        for (int i = 0; i < TEXTURE_UNITS; i++)
            shaderProgram.setInt("tex[" + i + "]", i);

        quadBuffer.setData(intermediateBuffer.flip());

        glBindVertexArray(quadBuffer.id());
        glDrawElements(GL_TRIANGLES, QuadBuffer.INDICES_PER_QUAD * insertedQuads, GL_UNSIGNED_INT, 0);

        intermediateBuffer.clear();
        insertedQuads = 0;
        camera = null;
    }

    public void delete() {
        quadBuffer.delete();
        shaderProgram.delete();
        MemoryUtil.memFree(intermediateBuffer);
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
}
