package com.game.transform;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public record Translation3D(float x, float y, float z) {
    public Translation3D() {
        this(0,0,0);
    }

    public Translation3D(Vector3fc v) {
        this(v.x(), v.y(), v.z());
    }

    Translation3D translate(Translation3D other) {
        return new Translation3D(x + other.x, y + other.y, z + other.z);
    }

    Translation3D translate(float x, float y, float z) {
        return new Translation3D(this.x + x, this.y + y, this.z + z);
    }

    Translation3D translate(Vector3fc other) {
        return new Translation3D(x + other.x(), y + other.y(), z + other.z());
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
