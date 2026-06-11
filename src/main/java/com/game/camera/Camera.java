package com.game.camera;

import com.game.transform.Transform;
import com.game.transform.Translation;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static com.game.transform.Rotation.WORLD_UP;

public class Camera {
    private CameraProjection projection;
    private Transform transform;

    public Camera(CameraProjection projection, Transform transform) {
        this.projection = projection;
        this.transform = transform;
    }

    public void move(Translation translation) {
        this.transform = this.transform.translate(translation);
    }

    public Matrix4f view() {
        Translation eye = transform.translation();
        Vector3f direction = transform.rotation().direction();

        return new Matrix4f().lookAlong(direction, WORLD_UP).translate(-eye.x(), -eye.y(), -eye.z());
    }

    public Matrix4f projection() {
        return projection.matrix();
    }

    public Matrix4f matrix() {
        return projection().mulAffine(view());
    }
}
