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

    @Override
    public boolean contains(Vec2f position) {
        return FloatUtils.geq(position.x(), left())
                && FloatUtils.leq(position.x(), right())
                && FloatUtils.geq(position.y(), bottom())
                && FloatUtils.leq(position.y(), top());
    }

    @Override
    public boolean staticIntersects(Shape other) {
        return switch (other) {
            case Rectangle rect -> {
                if (FloatUtils.gt(this.left(), rect.right())) yield false;
                if (FloatUtils.lt(this.right(), rect.left())) yield false;
                if (FloatUtils.gt(this.bottom(), rect.top())) yield false;
                yield !FloatUtils.lt(this.top(), rect.bottom());
            }
            case Circle circle -> {
                Vec2f rectCenter = this.center();
                Vec2f circleCenter = circle.center();
                float halfRectWidth = 0.5f * this.width();
                float halfRectHeight = 0.5f * this.height();
                float circleRadius = circle.radius();

                float dx = Math.abs(rectCenter.x() - circleCenter.x());
                float dy = Math.abs(rectCenter.y() - circleCenter.y());

                if (FloatUtils.gt(dx, halfRectWidth + circleRadius)) yield false;
                if (FloatUtils.gt(dy, halfRectHeight + circleRadius)) yield false;

                if (FloatUtils.leq(dx, halfRectWidth)) yield true;
                if (FloatUtils.leq(dy, halfRectHeight)) yield true;

                float leftoverX = dx - halfRectWidth;
                float leftoverY = dy - halfRectHeight;
                yield leftoverX * leftoverX + leftoverY * leftoverY <= circleRadius * circleRadius;
            }
        };
    }

    @Override
    public Shape moveTo(Vec2f position) {
        return new Rectangle(position, width, height);
    }

    @Override
    public Optional<IntersectionData> dynamicIntersection(Vec2f velocity, Shape other, Vec2f otherVelocity) {
        return switch (other) {
            case Rectangle rect2 -> {
                Vec2f relativeVelocity = velocity.sub(otherVelocity);
                if (relativeVelocity.equals(Vec2f.ZERO))
                    if (staticIntersects(other))
                        yield Optional.of(new IntersectionData(0, Vec2f.ZERO));
                    else
                        yield Optional.empty();

                Segment segment = Segment.fromDirection(center, relativeVelocity);
                float totalWidth = this.width + rect2.width;
                float totalHeight = this.height + rect2.height;
                Rectangle phantomRect = new Rectangle(rect2.center, totalWidth, totalHeight);

                yield segment.intersection(phantomRect);
            }
            case Circle circle -> circle.dynamicIntersection(otherVelocity, this, velocity)
                    .map(data -> new IntersectionData(data.t(), data.normal().negate()));
        };
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
