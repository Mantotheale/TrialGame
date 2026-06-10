package com.game.renderer;

import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

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

class QuadBuffer {
    public final static int VERTICES_PER_QUAD = 4;
    public final static int INDICES_PER_QUAD = 6;

    private final int arrayId;
    private final int vertexBufferId;
    private final int indexBufferId;

    private final int maxQuads;
    private final VertexLayout layout;

    public QuadBuffer(VertexLayout layout, int maxQuadCount) {
        maxQuads = maxQuadCount;
        this.layout = layout;

        arrayId = glGenVertexArrays();
        glBindVertexArray(arrayId);

        vertexBufferId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId);
        glBufferData(GL_ARRAY_BUFFER, (long) maxQuadCount * VERTICES_PER_QUAD * layout.size(), GL_DYNAMIC_DRAW);

        for (LayoutElement el : layout) {
            if (el.glType() == GL_INT) {
                glVertexAttribIPointer(el.index(), el.primitiveCount(), el.glType(), layout.size(), el.offset());
            } else {
                glVertexAttribPointer(el.index(), el.primitiveCount(), el.glType(), false, layout.size(), el.offset());
            }
            glEnableVertexAttribArray(el.index());
        }

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
        if (data.capacity() > maxQuads * VERTICES_PER_QUAD * layout.size()) throw new IllegalArgumentException("Buffer is too large (" + data.capacity() + "). The maximum is " + maxQuads);

        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId);
        glBufferSubData(GL_ARRAY_BUFFER, 0, data);
    }

    public void delete() {
        glDeleteVertexArrays(arrayId);
        glDeleteBuffers(vertexBufferId);
        glDeleteBuffers(indexBufferId);
    }
}
