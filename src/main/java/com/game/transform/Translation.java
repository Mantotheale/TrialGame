package com.game.transform;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public record Translation(float x, float y, float z) {
    public Translation() {
        this(0,0,0);
    }

    public Translation(Vector3fc v) {
        this(v.x(), v.y(), v.z());
    }

    Translation translate(Translation other) {
        return new Translation(x + other.x, y + other.y, z + other.z);
    }

    Translation translate(float x, float y, float z) {
        return new Translation(this.x + x, this.y + y, this.z + z);
    }

    Translation translate(Vector3fc other) {
        return new Translation(x + other.x(), y + other.y(), z + other.z());
    }

    public Vector3f vec() {
        return new Vector3f(x, y, z);
    }

    public Matrix4f matrix() {
        return new Matrix4f().setTranslation(x, y, z);
    }

    public Vector3f transform(Vector3fc vec) {
        return vec.add(x, y, z, new Vector3f());
    }
}
