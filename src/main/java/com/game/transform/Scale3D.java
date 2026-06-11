package com.game.transform;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public record Scale3D(float x, float y, float z) {
    public Scale3D(float s) {
        this(s,s,s);
    }

    public Scale3D() {
        this(1);
    }

    public Scale3D(Vector3fc v) {
        this(v.x(), v.y(), v.z());
    }

    public Vector3f vec() {
        return new Vector3f(x,y,z);
    }

    public Matrix4f matrix() {
        return new Matrix4f().scaling(x, y, z);
    }

    public Vector3f transform(Vector3fc vec) {
        return vec.mul(x, y, z, new Vector3f());
    }
}
