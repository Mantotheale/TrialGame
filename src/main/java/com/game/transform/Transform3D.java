package com.game.transform;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public record Transform3D(Translation3D translation3d, Rotation3D rotation3D, Scale3D scale3D) {
    public Transform3D() {
        this(new Translation3D(), new Rotation3D(), new Scale3D());
    }

    public Transform3D translate(Translation3D translation3d) {
        return new Transform3D(this.translation3d.translate(translation3d), rotation3D, scale3D);
    }

    public Transform3D translate(float x, float y, float z) {
        return new Transform3D(this.translation3d.translate(x, y, z), rotation3D, scale3D);
    }

    public Matrix4f matrix() {
        Matrix4f t = translation3d.matrix();
        Matrix4f r = rotation3D.matrix();
        Matrix4f s = scale3D.matrix();

        return t.mulAffine(r).mulAffine(s);
    }

    public Vector3f transform(Vector3fc vec) {
        return translation3d.transform(rotation3D.transform(scale3D.transform(vec)));
    }
}
