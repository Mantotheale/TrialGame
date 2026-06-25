package com.game.camera;

import com.game.event.bus.EventBus;
import com.game.event.instant.LateUpdateEvent;
import com.game.math.Vec2f;
import com.game.transform.Transform3D;
import com.game.transform.Translation3D;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.function.Supplier;

import static com.game.transform.Rotation3D.WORLD_UP;

public class Camera {
    private CameraProjection projection;
    private Transform3D transform3d;
    private Supplier<Vec2f> followingEntitySupplier;

    public Camera(CameraProjection projection, Transform3D transform3d, EventBus bus) {
        this.projection = projection;
        this.transform3d = transform3d;

        bus.addObserver(LateUpdateEvent.class, this::onLateUpdate);
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

    public void setEntityToFollow(Supplier<Vec2f> supplier) {
        this.followingEntitySupplier = supplier;
    }

    private void onLateUpdate(EventBus bus, LateUpdateEvent event) {
        if (followingEntitySupplier != null) {
            Vec2f entityPosition = followingEntitySupplier.get();

            transform3d = new Transform3D(
                    new Translation3D(
                            entityPosition.x(),
                            entityPosition.y(),
                            transform3d.translation3d().z()
                    ),
                    transform3d.rotation3D(),
                    transform3d.scale3D()
            );
        }
    }
}
