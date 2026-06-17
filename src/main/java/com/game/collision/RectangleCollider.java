package com.game.collision;

import com.game.util.FloatUtils;
import com.game.util.Vec2f;

import java.util.Optional;

public record RectangleCollider(Vec2f center, float width, float height, boolean isMobile) implements Collider {

    public RectangleCollider(Vec2f center, Vec2f dimensions, boolean isMobile) {
        this(center, dimensions.x(), dimensions.y(), isMobile);
    }

    @Override
    public Collider moveToPosition(Vec2f position) {
        return new RectangleCollider(position, width, height, isMobile);
    }

    @Override
    public boolean intersects(Collider other) {
        if (other instanceof RectangleCollider(Vec2f c2, float w2, float h2, _)) {
            if (c2.x() + (w2 / 2) < center.x() - (width / 2)) return false;
            if (c2.x() - (w2 / 2) > center.x() + (width / 2)) return false;
            if (c2.y() - (h2 / 2) > center.y() + (height / 2)) return false;
            return !(c2.y() + (h2 / 2) < center.y() - (height / 2));
        }
        return false;
    }

    @Override
    public float overlapArea(Collider other) {
        return axes(other).map(Axes::area).orElse(0f);
    }

    @Override
    public Optional<Vec2f> minimumTranslationVector(Vec2f beforeCenter, Collider other) {
        return axes(other).flatMap(a -> resolveMtv(beforeCenter, other, a));
    }

    private Optional<Axes> axes(Collider other) {
        if (!intersects(other)) return Optional.empty();
        if (other instanceof RectangleCollider(Vec2f c2, float w2, float h2, _)) {
            float x = (width + w2) / 2 - Math.abs(center.x() - c2.x());
            float y = (height + h2) / 2 - Math.abs(center.y() - c2.y());
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

            if (beforeYOverlap >= FloatUtils.EPSILON)
                return Optional.of(new Vec2f(beforeCenter.x() < c2.x() ? -xOverlap : xOverlap, 0));

            if (beforeXOverlap >= FloatUtils.EPSILON)
                return Optional.of(new Vec2f(0, beforeCenter.y() < c2.y() ? -yOverlap : yOverlap));

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