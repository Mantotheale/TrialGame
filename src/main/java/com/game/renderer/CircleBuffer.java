package com.game.renderer;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.GL_INT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;

public class CircleBuffer {
    private final int arrayId;
    private final int vertexBufferId;

    private final int maxCircles;
    private final VertexLayout layout;

    public CircleBuffer(int maxCircles) {
        this.maxCircles = maxCircles;

        this.layout = new VertexLayout.Builder()
                .pushFloats(2)
                .pushFloats(1)
                .pushFloats(4)
                .build();

        arrayId = glGenVertexArrays();
        glBindVertexArray(arrayId);

        vertexBufferId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId);
        glBufferData(GL_ARRAY_BUFFER, (long) maxCircles * layout.size(), GL_DYNAMIC_DRAW);

        for (LayoutElement el : layout) {
            if (el.glType() == GL_INT) {
                glVertexAttribIPointer(el.index(), el.primitiveCount(), el.glType(), layout.size(), el.offset());
            } else {
                glVertexAttribPointer(el.index(), el.primitiveCount(), el.glType(), false, layout.size(), el.offset());
            }
            glEnableVertexAttribArray(el.index());
        }
    }

    public int id() {
        return arrayId;
    }

    public void setData(ByteBuffer data) {
        if (data.capacity() > maxCircles * layout.size()) throw new IllegalArgumentException("Buffer is too large (" + data.capacity() + "). The maximum is " + maxCircles * layout.size());

        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId);
        glBufferSubData(GL_ARRAY_BUFFER, 0, data);
    }

    public void delete() {
        glDeleteVertexArrays(arrayId);
        glDeleteBuffers(vertexBufferId);
    }
}