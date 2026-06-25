package com.game.collision;

import com.game.math.Vec2f;

public enum CollisionType {
    ELASTIC {
        @Override
        public Vec2f resolveVelocity(Vec2f startingVelocity, Vec2f normal) {
            return startingVelocity.reflect(normal);
        }
    },
    INELASTIC {
        @Override
        public Vec2f resolveVelocity(Vec2f startingVelocity, Vec2f normal) {
            return startingVelocity.reject(normal);
        }
    };

    public abstract Vec2f resolveVelocity(Vec2f startingVelocity, Vec2f normal);
}
