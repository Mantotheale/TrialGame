package com.game.transform;

import org.joml.Matrix4f;

public record Transform(Translation translation, Rotation rotation, Scale scale) {
    public Transform() {
        this(new Translation(), new Rotation(), new Scale());
    }

    public Transform translate(Translation translation) {
        return new Transform(this.translation.translate(translation), rotation, scale);
    }

    public Transform translate(float x, float y, float z) {
        return new Transform(this.translation.translate(x, y, z), rotation, scale);
    }

    public Matrix4f matrix() {
        Matrix4f t = translation.matrix();
        Matrix4f r = rotation.matrix();
        Matrix4f s = scale.matrix();

        return t.mulAffine(r).mulAffine(s);
    }
}
