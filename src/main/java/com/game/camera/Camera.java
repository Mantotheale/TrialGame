package com.game.camera;

import com.game.transform.Transform3D;
import com.game.transform.Translation3D;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static com.game.transform.Rotation3D.WORLD_UP;

public class Camera {
    private CameraProjection projection;
    private Transform3D transform3d;

    public Camera(CameraProjection projection, Transform3D transform3d) {
        this.projection = projection;
        this.transform3d = transform3d;
    }

    public void move(Translation3D translation3d) {
        this.transform3d = this.transform3d.translate(translation3d);
    }

    public Matrix4f view() {
        Translation3D eye = transform3d.translation3d();
        Vector3f direction = transform3d.rotation3D().direction();

        return new Matrix4f().lookAlong(direction, WORLD_UP).translate(-eye.x(), -eye.y(), -eye.z());
    }

    public Matrix4f projection() {
        return projection.matrix();
    }

    public Matrix4f matrix() {
        return projection().mulAffine(view());
    }
}
