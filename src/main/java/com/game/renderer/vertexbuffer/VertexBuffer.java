package com.game.renderer.vertexbuffer;

public sealed interface VertexBuffer permits Buffer, NoBuffer {
    int id();

    static VertexBuffer create(int id) {
        return new Buffer(id);
    }
}

record Buffer(int id) implements VertexBuffer {
    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "VertexBuffer( " + id + ")";
    }
}

record NoBuffer() implements VertexBuffer {
    @Override
    public int id() {
        return 0;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return "NoVertexBuffer";
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof NoBuffer;
    }
}