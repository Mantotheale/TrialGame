package com.game.camera;

import com.game.event.Event;
import com.game.event.EventObserver;
import com.game.event.StartUpdateEvent;
import com.game.input.InputManager;
import com.game.input.rawcomponents.KeyState;
import com.game.input.rawcomponents.PhysicalKey;
import com.game.transform.Transform3D;
import com.game.transform.Translation3D;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static com.game.Game.UPDATE_TIME;
import static com.game.transform.Rotation3D.WORLD_UP;

public class Camera implements EventObserver {
    private CameraProjection projection;
    private Transform3D transform3d;
    private final InputManager inputManager;

    public Camera(CameraProjection projection, Transform3D transform3d, InputManager inputManager) {
        this.projection = projection;
        this.transform3d = transform3d;
        this.inputManager = inputManager;
    }

    private void move(Translation3D translation3d) {
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

    @Override
    public void onEvent(Event event) {
        if (event instanceof StartUpdateEvent) {
            if (inputManager.keyState(PhysicalKey.UP) == KeyState.DOWN)
                move(new Translation3D(0, 2 * (float) UPDATE_TIME, 0));
            if (inputManager.keyState(PhysicalKey.DOWN) == KeyState.DOWN)
                move(new Translation3D(0, -2 * (float) UPDATE_TIME, 0));
            if (inputManager.keyState(PhysicalKey.LEFT) == KeyState.DOWN)
                move(new Translation3D(-2 * (float) UPDATE_TIME, 0, 0));
            if (inputManager.keyState(PhysicalKey.RIGHT) == KeyState.DOWN)
                move(new Translation3D(2 * (float) UPDATE_TIME, 0, 0));
        }
    }
}
