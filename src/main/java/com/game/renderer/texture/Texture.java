package com.game.renderer.texture;

public sealed interface Texture permits Tex, NoTex {
    int id();

    static Texture create(int id) {
        return new Tex(id);
    }
}

record Tex(int id) implements Texture {
    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "Texture( " + id + ")";
    }
}

record NoTex() implements Texture {
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
        return "NoTexture";
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof NoTex;
    }
}