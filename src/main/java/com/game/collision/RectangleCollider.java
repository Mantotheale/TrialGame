package com.game.collision;

import com.game.util.FloatUtils;
import com.game.util.Vec2f;

import java.util.Optional;

public record RectangleCollider(Vec2f center, float width, float height, boolean isMobile) implements Collider {

    public RectangleCollider(Vec2f center, Vec2f dimensions, boolean isMobile) {
        this(center, dimensions.x(), dimensions.y(), isMobile);
    }

    public float left() {
        return center.x() - width / 2;
    }

    public float right() {
        return center.x() + width / 2;
    }

    public float top() {
        return center.y() + height / 2;
    }

    public float bottom() {
        return center.y() - height / 2;
    }

    @Override
    public Collider moveToPosition(Vec2f position) {
        return new RectangleCollider(position, width, height, isMobile);
    }

    @Override
    public boolean intersects(Collider other) {
        return switch (other) {
            case RectangleCollider(Vec2f c2, float w2, float h2, _) -> {
                if (c2.x() + (w2 / 2) < center.x() - (width / 2)) yield false;
                if (c2.x() - (w2 / 2) > center.x() + (width / 2)) yield false;
                if (c2.y() - (h2 / 2) > center.y() + (height / 2)) yield false;
                yield !(c2.y() + (h2 / 2) < center.y() - (height / 2));
            }
            case CircleCollider circ -> ColliderUtils.intersect(this, circ);
        };
    }

    @Override
    public float overlapArea(Collider other) {
        return axes(other).map(Axes::area).orElse(0f);
    }

    @Override
    public Optional<Vec2f> minimumTranslationVector(Vec2f beforeCenter, Collider other) {
        return axes(other).flatMap(a -> resolveMtv(beforeCenter, other, a));
    }

    @Override
    public boolean contains(Vec2f point) {
        return point.x() >= left() && point.x() <= right() && point.y() >= bottom() && point.y() <= top();
    }

    private Optional<Axes> axes(Collider other) {
        if (!intersects(other)) return Optional.empty();
        if (other instanceof RectangleCollider(Vec2f c2, float w2, float h2, _)) {
            float x = (width + w2) / 2 - Math.abs(center.x() - c2.x());
            float y = (height + h2) / 2 - Math.abs(center.y() - c2.y());
            System.out.println("player center: " + center + " overlap axes " + x + ", " + y);
            return Optional.of(new Axes(x, y));
        }
        return Optional.empty();
    }

    private Optional<Vec2f> resolveMtv(Vec2f beforeCenter, Collider other, Axes axes) {
        if (other instanceof RectangleCollider(Vec2f c2, float w2, float h2, _)) {
            float xOverlap = axes.x;
            float yOverlap = axes.y;

            if (FloatUtils.areEqualsEps(xOverlap, 0) || FloatUtils.areEqualsEps(yOverlap, 0))
                return Optional.of(Vec2f.ZERO);

            float beforeXOverlap = (width + w2) / 2 - Math.abs(beforeCenter.x() - c2.x());
            float beforeYOverlap = (height + h2) / 2 - Math.abs(beforeCenter.y() - c2.y());
            System.out.println("before overlap axes " + beforeXOverlap + " " + beforeYOverlap);

            if (beforeYOverlap >= FloatUtils.EPSILON) {
                float dxCenters = center.x() - c2.x();
                float necessaryDistance = 0.5f * (width + w2);

                if (beforeCenter.x() <= c2.x()) {
                    // Coming from left
                    float xDisplacement = necessaryDistance + dxCenters;
                    return Optional.of(new Vec2f(-xDisplacement, 0));
                } else {
                    // Coming from right
                    float xDisplacement = necessaryDistance - dxCenters;
                    return Optional.of(new Vec2f(xDisplacement, 0));
                }
            }

            if (beforeXOverlap >= FloatUtils.EPSILON) {
                float dyCenters = center.y() - c2.y();
                float necessaryDistance = 0.5f * (height + h2);

                if (beforeCenter.y() <= c2.y()) {
                    // Coming from down
                    float yDisplacement = necessaryDistance + dyCenters;
                    return Optional.of(new Vec2f(0, -yDisplacement));
                } else {
                    // Coming from up
                    float yDisplacement = necessaryDistance - dyCenters;
                    return Optional.of(new Vec2f(0, yDisplacement));
                }
            }

            // Should never execute
            System.out.println("Problem");
            if (xOverlap < yOverlap)
                return Optional.of(new Vec2f(center.x() < c2.x() ? -xOverlap : xOverlap, 0));
            else
                return Optional.of(new Vec2f(0, center.y() < c2.y() ? -yOverlap : yOverlap));
        }
        return Optional.empty();
    }

    private record Axes(float x, float y) {
        float area() {
            return x * y;
        }
    }
}