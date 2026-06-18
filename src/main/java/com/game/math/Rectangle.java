package com.game.math;

import com.game.collision.RectangleCollider;

import java.util.Optional;

public record Rectangle(float centerX, float centerY, float width, float height) {
    public Rectangle {
        if (width <= 0 || height <= 0)
            throw new IllegalArgumentException("The dimensions of a rectangle should be positive");
    }

    public Rectangle(Vec2f center, float width, float height) {
        this(center.x(), center.y(), width, height);
    }

    public Rectangle(Vec2f center, Vec2f dimensions) {
        this(center.x(), center.y(), dimensions.x(), dimensions.y());
    }

    public Vec2f center() {
        return new Vec2f(centerX, centerY);
    }

    public float left() {
        return centerX - 0.5f * width;
    }

    public float right() {
        return centerX + 0.5f * width;
    }

    public float bottom() {
        return centerY - 0.5f * height;
    }

    public float top() {
        return centerY + 0.5f * height;
    }

    public boolean contains(Vec2f point) {
        return point.x() >= left() && point.x() <= right() && point.y() >= bottom() && point.y() <= top();
    }

    public boolean intersects(Rectangle other) {
        if (other.right() < this.left()) return false;
        if (other.left() > this.right()) return false;
        if (other.bottom() > this.top()) return false;
        return  !(other.top() < this.bottom());
    }

    public Optional<Float> intersectionTime(Rectangle other, Vec2f destination) {
        float dx = destination.x() - this.centerX;
        float dy = destination.y() - this.centerY;
        float px = other.centerX - this.centerX;
        float py = other.centerY - this.centerY;

        float sumHalfWidths = 0.5f * (this.width + other.width);
        float sumHalfHeights = 0.5f * (this.height + other.height);

        if (FloatUtils.areEqualsEps(dx, 0) && FloatUtils.areEqualsEps(dy, 0))
            if (px >= -sumHalfWidths && px <= sumHalfWidths && py >= -sumHalfHeights && py <= sumHalfHeights)
                return Optional.of(0f);
            else
                return Optional.empty();

        if (FloatUtils.areEqualsEps(dx, 0)) {
            if (px < -sumHalfWidths || px > sumHalfWidths)
                return Optional.empty();

            float tyMin = (py - sumHalfHeights) / dy;
            float tyMax = (py + sumHalfHeights) / dy;
            FloatRange rangeY = new FloatRange(Math.min(tyMin, tyMax), Math.max(tyMin, tyMax));
            return rangeY.intersection(FloatRange.ZERO_ONE).map(FloatRange::min);
        }

        if (FloatUtils.areEqualsEps(dy, 0)) {
            if (py < -sumHalfHeights || py > sumHalfHeights)
                return Optional.empty();

            float txMin = (px - sumHalfWidths) / dx;
            float txMax = (px + sumHalfWidths) / dx;
            FloatRange rangeX = new FloatRange(Math.min(txMin, txMax), Math.max(txMin, txMax));
            return rangeX.intersection(FloatRange.ZERO_ONE).map(FloatRange::min);
        }

        float txMin = (px - sumHalfWidths) / dx;
        float txMax = (px + sumHalfWidths) / dx;
        FloatRange rangeX = new FloatRange(Math.min(txMin, txMax), Math.max(txMin, txMax));

        float tyMin = (py - sumHalfHeights) / dy;
        float tyMax = (py + sumHalfHeights) / dy;
        FloatRange rangeY = new FloatRange(Math.min(tyMin, tyMax), Math.max(tyMin, tyMax));

        return rangeX.intersection(rangeY)
                .flatMap(r -> r.intersection(FloatRange.ZERO_ONE))
                .map(FloatRange::min);
    }

    public Rectangle stopAtTime(Vec2f destination, float time) {
        Vec2f displacement = destination.subtract(center()).mul(time);
        return toPosition(center().add(displacement));
    }

    public Rectangle toPosition(Vec2f newCenter) {
        return new Rectangle(newCenter, this.width, this.height);
    }
}
