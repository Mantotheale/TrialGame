package com.game.renderer.shaderprogram;

public sealed interface ShaderProgram permits Program, NoProgram {
    int id();

    static ShaderProgram create(int id) {
        return new Program(id);
    }
}

record Program(int id) implements ShaderProgram {
    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "ShaderProgram( " + id + ")";
    }
}

record NoProgram() implements ShaderProgram {
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
        return "NoShaderProgram";
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof NoProgram;
    }
}