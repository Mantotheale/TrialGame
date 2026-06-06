package com.game.renderer.vertexarray;

public sealed interface VertexArray {
    int id();

    static VertexArray create(int id) {
        return new Array(id);
    }
}

record Array(int id) implements VertexArray {
    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "VertexArray( " + id + ")";
    }
}

record NoArray() implements VertexArray {
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
        return "NoVertexArray";
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof NoArray;
    }
}