package com.game.math;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
        return switch (other) {
            case Circle(Vec2f c2, float r2) -> {
                float radiiSum = this.radius + r2;
                yield this.center.squaredDistance(c2) <= radiiSum * radiiSum;
            }
            case Rectangle rect -> rect.staticIntersects(this);
        };
    }

    @Override
    public Optional<IntersectionData> dynamicIntersection(Vec2f velocity, Shape other, Vec2f otherVelocity) {
        Vec2f relativeVelocity = velocity.sub(otherVelocity);

        if (relativeVelocity.equals(Vec2f.ZERO))
            if (staticIntersects(other))
                return Optional.of(new IntersectionData(0, Vec2f.ZERO));
            else
                return Optional.empty();

        return switch (other) {
            case Circle circle2 -> {
                float radiiSum = this.radius + circle2.radius;
                Circle phantomCircle = new Circle(circle2.center, radiiSum);

                yield Segment.fromDirection(this.center, relativeVelocity).intersection(phantomCircle);
            }
            case Rectangle rect -> {
                Segment segment = Segment.fromDirection(center, relativeVelocity);
                List<IntersectionData> intersections = new ArrayList<>();

                float sumW = (radius * 2) + rect.width();
                float sumH = (radius * 2) + rect.height();
                Rectangle phantomRect = new Rectangle(rect.center(), sumW, sumH);
                Optional<IntersectionData> rectIntersect = segment.intersection(phantomRect);
                rectIntersect.ifPresent(x -> {
                    Vec2f point = center.add(relativeVelocity.mul(x.t()));

                    boolean isFlatEdgeHit =
                            (FloatUtils.geq(point.x(), rect.left()) && FloatUtils.leq(point.x(), rect.right())) ||
                                    (FloatUtils.geq(point.y(), rect.bottom()) && FloatUtils.leq(point.y(), rect.top()));

                    if (isFlatEdgeHit)
                        intersections.add(x);
                });

                Circle phantomCircle = new Circle(new Vec2f(rect.right(), rect.top()), radius);
                Optional<IntersectionData> circIntersect = segment.intersection(phantomCircle);
                circIntersect.ifPresent(x -> {
                    Vec2f point = center.add(relativeVelocity.mul(x.t()));
                    if (FloatUtils.geq(point.x(), rect.right()) && FloatUtils.geq(point.y(), rect.top()))
                        intersections.add(x);
                });

                phantomCircle = new Circle(new Vec2f(rect.right(), rect.bottom()), radius);
                circIntersect = segment.intersection(phantomCircle);
                circIntersect.ifPresent(x -> {
                    Vec2f point = center.add(relativeVelocity.mul(x.t()));
                    if (FloatUtils.geq(point.x(), rect.right()) && FloatUtils.leq(point.y(), rect.bottom()))
                        intersections.add(x);
                });

                phantomCircle = new Circle(new Vec2f(rect.left(), rect.bottom()), radius);
                circIntersect = segment.intersection(phantomCircle);
                circIntersect.ifPresent(x -> {
                    Vec2f point = center.add(relativeVelocity.mul(x.t()));
                    if (FloatUtils.leq(point.x(), rect.left()) && FloatUtils.leq(point.y(), rect.bottom()))
                        intersections.add(x);
                });

                phantomCircle = new Circle(new Vec2f(rect.left(), rect.top()), radius);
                circIntersect = segment.intersection(phantomCircle);
                circIntersect.ifPresent(x -> {
                    Vec2f point = center.add(relativeVelocity.mul(x.t()));
                    if (FloatUtils.leq(point.x(), rect.left()) && FloatUtils.geq(point.y(), rect.top()))
                        intersections.add(x);
                });

                yield intersections.stream().min(Comparator.naturalOrder());
            }
        };
    }
}
