package com.game.math;

import java.util.Optional;

public record Circle(Vec2f center, float radius) implements Shape {
    public Circle {
        if (radius <= 0)
            throw new IllegalArgumentException("The radius of a circle should be positive. Was " + radius);
    }

    @Override
    public Shape moveTo(Vec2f position) {
        return new Circle(position, radius);
    }

    @Override
    public boolean contains(Vec2f position) {
        return FloatUtils.leq(center.squaredDistance(position), radius * radius);
    }

    @Override
    public boolean staticIntersects(Shape other) {
        if (other instanceof Circle(Vec2f c2, float r2)) {
            float radiiSum = this.radius + r2;
            return this.center.squaredDistance(c2) <= radiiSum * radiiSum;
        }

        return false;

        //throw new IllegalArgumentException("UNKNOWN SHAPE");
    }

    @Override
    public Optional<IntersectionData> dynamicIntersection(Vec2f velocity, Shape other) {
        if (other instanceof Circle circle2) {
            if (velocity.equals(Vec2f.ZERO)) {
                if (staticIntersects(circle2))
                    return Optional.of(new IntersectionData(0, Vec2f.ZERO));
                else
                    return Optional.empty();
            }

            float radiiSum = this.radius + circle2.radius;
            Circle phantomCircle = new Circle(circle2.center, radiiSum);

            return Segment.fromDirection(this.center, velocity).intersection(phantomCircle);
        }

        return Optional.empty();
    }
}
