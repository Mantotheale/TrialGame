package com.game.camera;

import com.game.Entity;
import com.game.Reshiram;
import com.game.event.EntityMovedEvent;
import com.game.event.Event;
import com.game.event.EventDispatcher;
import com.game.event.EventObserver;
import com.game.transform.Transform3D;
import com.game.transform.Translation3D;
import com.game.util.Vec2f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static com.game.transform.Rotation3D.WORLD_UP;

public class Camera implements EventObserver {
    private CameraProjection projection;
    private Transform3D transform3d;

    public Camera(CameraProjection projection, Transform3D transform3d) {
        this.projection = projection;
        this.transform3d = transform3d;
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

    @Override
    public void onEvent(EventDispatcher dispatcher, Event event) {
        if (event instanceof EntityMovedEvent(Entity entity, Vec2f position)) {
            if (entity instanceof Reshiram) {
                transform3d = new Transform3D(
                        new Translation3D(
                                position.x(),
                                position.y(),
                                transform3d.translation3d().z()
                        ),
                        transform3d.rotation3D(),
                        transform3d.scale3D()
                );
            }
        }
    }
}
