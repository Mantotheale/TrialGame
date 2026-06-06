package com.game.renderer.indexbuffer;

public sealed interface IndexBuffer permits Buffer, NoBuffer {
    int id();

    static IndexBuffer create(int id) {
        return new Buffer(id);
    }
}

record Buffer(int id) implements IndexBuffer {
    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "IndexBuffer( " + id + ")";
    }
}

record NoBuffer() implements IndexBuffer {
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
        return "NoIndexBuffer";
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof NoBuffer;
    }
}