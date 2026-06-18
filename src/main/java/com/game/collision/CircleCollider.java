package com.game.collision;

import com.game.math.Vec2f;

import java.util.List;
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
                if (squaredDistance <= sumOfRadii * sumOfRadii) System.out.println("eccoci");
                yield squaredDistance <= sumOfRadii * sumOfRadii;
            }
            case RectangleCollider rect -> ColliderUtils.intersect(rect, this);
        };
    }

    @Override
    public float overlapArea(Collider other) {/*
        switch (other) {
            case CircleCollider(Vec2f c2, float r2, _) -> {
                float squaredDistance = this.center.squaredDistance(c2);
                float sumOfRadii = this.radius + r2;
                return Math.max(0, sumOfRadii * sumOfRadii - squaredDistance);
            }
            case RectangleCollider(Vec2f c2, float w2, float h2, _) -> {
                float squaredDistance = this.center.squaredDistance(c2);
                float sumOfRadii = this.radius + 0.5f * (w2 + h2);
                return Math.max(0, sumOfRadii * sumOfRadii - squaredDistance);
            }
        }*/
        return 0;
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
                    List<Vec2f> intersections = ColliderUtils.lineToRectIntersections(center, beforeCenter, rect);
                    System.out.println(intersections);

                    ColliderUtils.RectOutProjection rectOutProjection = ColliderUtils.projectToRectPerimeter(rect, this.center);
                    Vec2f toPerimeterDisplacement = rectOutProjection.intersection().subtract(center);
                    Vec2f fromPerimeterDisplacement = rectOutProjection.outDirection().mul(radius);
                    Vec2f displacement = toPerimeterDisplacement.add(fromPerimeterDisplacement);
                    return Optional.of(displacement);
                }

                Vec2f rectClosePoint = ColliderUtils.closestPointInRect(rect, this.center);
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
