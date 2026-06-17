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
        return new RectangleCollider(position, width, height,  isMobile);
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
    public float intersectionArea(Collider other) {
        if (!intersects(other)) return 0;

        if (other instanceof RectangleCollider(Vec2f c2, float w2, float h2, _)) {
            float xOverlap = (width + w2) / 2 - Math.abs(center.x() - c2.x());
            float yOverlap = (height + h2) / 2 - Math.abs(center.y() - c2.y());

            return xOverlap * yOverlap;
        } else {
            throw new IllegalArgumentException("Unknown collider type");
        }
    }

    @Override
    public Optional<Vec2f> minimumTranslationVector(Collider before, Collider other) {
        if (!intersects(other)) return Optional.empty();

        if (other instanceof RectangleCollider(Vec2f c2, float w2, float h2, _)) {
            float xOverlap = (width + w2) / 2 - Math.abs(center.x() - c2.x());
            float yOverlap = (height + h2) / 2 - Math.abs(center.y() - c2.y());

            if (FloatUtils.areEqualsEps(xOverlap, 0) || FloatUtils.areEqualsEps(yOverlap, 0))
                return Optional.of(Vec2f.ZERO);

            if (before instanceof RectangleCollider(Vec2f beforeC, _, _, _)) {
                float beforeXOverlap = (width + w2) / 2 - Math.abs(beforeC.x() - c2.x());
                float beforeYOverlap = (height + h2) / 2 - Math.abs(beforeC.y() - c2.y());

                if (beforeYOverlap >= FloatUtils.EPSILON) {
                    if (beforeC.x() < c2.x())
                        return Optional.of(new Vec2f(-xOverlap, 0));
                    else
                        return Optional.of(new Vec2f(xOverlap, 0));
                } else if (beforeXOverlap >= FloatUtils.EPSILON) {
                    if (beforeC.y() < c2.y())
                        return Optional.of(new Vec2f(0, -yOverlap));
                    else
                        return Optional.of(new Vec2f(0, yOverlap));
                }

                if (xOverlap < yOverlap) {
                    if (center.x() < c2.x()) return Optional.of(new Vec2f(-xOverlap, 0));
                    else return Optional.of(new Vec2f(xOverlap, 0));
                } else {
                    if (center.y() < c2.y()) return Optional.of(new Vec2f(0, -yOverlap));
                    else return Optional.of(new Vec2f(0, yOverlap));
                }
            } else {
                throw new IllegalArgumentException("The collider has changed types");
            }
        }

        return Optional.empty();
    }
}
