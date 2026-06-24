package com.game.math;

import java.util.Optional;

public record Rectangle(Vec2f center, float width, float height) implements Shape {
    public Rectangle {
        if (width <= 0)
            throw new IllegalArgumentException("The width of a rectangle should be positive. Was " + width);

        if (height <= 0)
            throw new IllegalArgumentException("The height of a rectangle should be positive. Was " + height);
    }

    public Rectangle(Vec2f center, Vec2f dimensions) {
        this(center, dimensions.x(), dimensions.y());
    }

    public float left() {
        return center.x() - 0.5f * width;
    }

    public float right() {
        return center.x() + 0.5f * width;
    }

    public float bottom() {
        return center.y() - 0.5f * height;
    }

    public float top() {
        return center.y() + 0.5f * height;
    }

    public boolean staticIntersects(Rectangle other) {
        if (FloatUtils.gt(this.left(), other.right())) return false;
        if (FloatUtils.lt(this.right(), other.left())) return false;
        if (FloatUtils.gt(this.bottom(), other.top())) return false;
        return !FloatUtils.lt(this.top(), other.bottom());
    }

    @Override
    public Shape moveTo(Vec2f position) {
        return new Rectangle(position, width, height);
    }

    public Optional<IntersectionData> dynamicIntersection(Vec2f velocity, Shape other) {
        if (other instanceof Rectangle rect2) {
            if (velocity.equals(Vec2f.ZERO))
                if (staticIntersects(rect2))
                    return Optional.of(new IntersectionData(0, Vec2f.ZERO));
                else
                    return Optional.empty();

            Segment segment = Segment.fromDirection(center, velocity);
            float totalWidth = this.width + rect2.width;
            float totalHeight = this.height + rect2.height;
            Rectangle phantomRect = new Rectangle(rect2.center, totalWidth, totalHeight);

            return segment.intersection(phantomRect);
        } else {
            throw new IllegalArgumentException("UNKNOW SHAPE");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        return (obj instanceof Rectangle(Vec2f c2, float w2, float h2))
                && this.center.equals(c2)
                && FloatUtils.eq(this.width, w2)
                && FloatUtils.eq(this.height, h2);
    }
}
