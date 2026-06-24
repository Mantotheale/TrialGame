package com.game.collision;

import com.game.math.*;

import java.util.Optional;

public record Collider(Shape shape, boolean isFixed) {
    public Vec2f center() {
        return shape.center();
    }

    public Collider moveTo(Vec2f position) {
        return new Collider(shape.moveTo(position), isFixed);
    }

    public Optional<IntersectionData> dynamicIntersection(Vec2f velocity, Collider other) {
        return shape.dynamicIntersection(velocity, other.shape());
    }

    public Vec2f resolveCollision(Vec2f initialVelocity, IntersectionData intersectionData) {
        return IntersectionUtils.resolveDynamicIntersection(initialVelocity, intersectionData);
    }

    float squaredDistance(Collider other) {
        return this.center().squaredDistance(other.center());

    }
}
