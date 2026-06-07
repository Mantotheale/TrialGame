package com.game.renderer;

import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class QuadArrayBuffer {
    private final int arrayId;
    private final int vertexBufferId;
    private final int indexBufferId;

    public final static int VERTICES_PER_QUAD = 4;
    public final static int INDICES_PER_QUAD = 6;

    public QuadArrayBuffer(int maxQuadCount, int vertexByteSize) {
        arrayId = glGenVertexArrays();
        glBindVertexArray(arrayId);

        vertexBufferId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId);
        glBufferData(GL_ARRAY_BUFFER, (long) maxQuadCount * VERTICES_PER_QUAD * vertexByteSize, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, vertexByteSize, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, vertexByteSize, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        IntBuffer buffer = MemoryUtil.memAllocInt(maxQuadCount * INDICES_PER_QUAD);
        for (int quad = 0; quad < maxQuadCount; quad++) {
            int indexInVertexBuffer = quad * VERTICES_PER_QUAD;
            buffer
                    .put(indexInVertexBuffer)
                    .put(indexInVertexBuffer + 1)
                    .put(indexInVertexBuffer + 2)
                    .put(indexInVertexBuffer)
                    .put(indexInVertexBuffer + 2)
                    .put(indexInVertexBuffer + 3);
        }
        buffer.flip();

        indexBufferId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
    }

    public int id() {
        return arrayId;
    }

    public void setData(ByteBuffer data) {
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId);
        glBufferSubData(GL_ARRAY_BUFFER, 0, data);
    }

    public void setData(FloatBuffer data) {
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId);
        glBufferSubData(GL_ARRAY_BUFFER, 0, data);
    }

    public void delete() {
        glDeleteVertexArrays(arrayId);
        glDeleteBuffers(vertexBufferId);
        glDeleteBuffers(indexBufferId);
    }
}
