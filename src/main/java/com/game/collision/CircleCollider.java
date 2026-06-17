package com.game.collision;

import com.game.util.Vec2f;

import java.util.Optional;

public record CircleCollider(Vec2f center, float radius, boolean isMobile) implements Collider {
    @Override
    public Collider moveToPosition(Vec2f position) {
        return new CircleCollider(position, radius, isMobile);
    }

    @Override
    public boolean intersects(Collider other) {
        return switch (other) {
            case CircleCollider(Vec2f c2, float r2, _) -> {
                float squaredDistance = this.center.squaredDistance(c2);
                float sumOfRadii = this.radius + r2;
                yield squaredDistance <= sumOfRadii * sumOfRadii;
            }
            case RectangleCollider rect -> ColliderUtils.intersect(rect, this);
        };
    }

    @Override
    public float overlapArea(Collider other) {
        if (other instanceof CircleCollider(Vec2f c2, float r2, _)) {
            float squaredDistance = this.center.squaredDistance(c2);
            float sumOfRadii = this.radius + r2;
            return Math.max(0, sumOfRadii * sumOfRadii - squaredDistance);
        }
        throw new IllegalArgumentException("Unknown collider type");
    }

    @Override
    public Optional<Vec2f> minimumTranslationVector(Vec2f beforeCenter, Collider other) {
        if (!intersects(other)) return Optional.empty();

        switch (other) {
            case CircleCollider(Vec2f c2, float r2, _) -> {
                float distance = this.center.distance(c2);
                float sumOfRadii = this.radius + r2;
                float overlap = sumOfRadii - distance;

                Vec2f translationDirection = this.center.subtract(c2).normalize();
                return Optional.of(translationDirection.mul(overlap));
            }
            case RectangleCollider rect -> {
                if (rect.contains(center)) {
                    throw new IllegalStateException("Il cerchio si è avvicinato troppo, DA PROGRAMMARE");
                }

                Vec2f rectClosePoint = ColliderUtils.rectClosePoint(rect, this);
                float distance = this.center.distance(rectClosePoint);
                float overlap = radius - distance;

                Vec2f translationDirection = this.center.subtract(rectClosePoint).normalize();
                return Optional.of(translationDirection.mul(overlap));
            }
        }
    }

    @Override
    public boolean contains(Vec2f point) {
        return point.squaredDistance(center) <= radius * radius;
    }
}
