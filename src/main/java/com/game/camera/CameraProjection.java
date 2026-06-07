package com.game.camera;

import org.joml.Matrix4f;

public sealed interface CameraProjection {
    Matrix4f matrix();

    record Orthographic(float left, float right, float top, float bottom, float near, float far) implements CameraProjection {
        @Override
        public Matrix4f matrix() {
            return new Matrix4f().setOrtho(left, right, top, bottom, near, far);
        }
    }

    record Perspective(float aspect, float near, float far, float fov) implements CameraProjection {
        @Override
        public Matrix4f matrix() {
            return new Matrix4f().setPerspective(fov, aspect, near, far);
        }

        public Perspective fromDegrees(float aspect, float near, float far, float fovDegrees) {
            return new Perspective(aspect, near, far, (float) Math.toRadians(fovDegrees));
        }
    }
}
