package com.game.math;

import java.util.Optional;

public record Segment(Vec2f origin, Vec2f destination) {
    public Segment {
        if (origin.equals(destination))
            throw new IllegalArgumentException("Origin and destination of a segment can't be the same");
    }

    public Vec2f direction() {
        return destination.sub(origin);
    }

    public Optional<IntersectionData> intersection(Rectangle rect) {
        Vec2f dir = direction();

        float txStart, txEnd;
        if (FloatUtils.eq(dir.x(), 0)) {
            if (FloatUtils.leq(origin.x(), rect.left()) || FloatUtils.geq(origin.x(), rect.right()))
                return Optional.empty();

            txStart = Float.NEGATIVE_INFINITY;
            txEnd = Float.POSITIVE_INFINITY;
        } else {
            float inverseDx = 1 / dir.x();
            float tx1 = inverseDx * (rect.left() - origin.x());
            float tx2 = inverseDx * (rect.right() - origin.x());
            txStart = Math.min(tx1, tx2);
            txEnd = Math.max(tx1, tx2);
        }

        float tyStart, tyEnd;
        if (FloatUtils.eq(dir.y(), 0)) {
            if (FloatUtils.leq(origin.y(), rect.bottom()) || FloatUtils.geq(origin.y(), rect.top()))
                return Optional.empty();

            tyStart = Float.NEGATIVE_INFINITY;
            tyEnd = Float.POSITIVE_INFINITY;
        } else {
            float inverseDy = 1 / dir.y();
            float ty1 = inverseDy * (rect.bottom() - origin.y());
            float ty2 = inverseDy * (rect.top() - origin.y());
            tyStart = Math.min(ty1, ty2);
            tyEnd = Math.max(ty1, ty2);
        }

        if (FloatUtils.gt(txStart, tyEnd) || FloatUtils.gt(tyStart, txEnd))
            return Optional.empty();

        float tStart = Math.max(txStart, tyStart);
        float tEnd = Math.min(txEnd, tyEnd);
        if (tEnd < 0) return Optional.empty();
        if (FloatUtils.lt(tStart, 0) || FloatUtils.gt(tStart, 1)) return Optional.empty();

        Vec2f normal;
        if (FloatUtils.eq(tStart, tEnd))
            normal = Vec2f.ZERO;
        else if (FloatUtils.eq(txStart, tyStart))
            normal = dir.negate().normalize();
        else if (FloatUtils.gt(txStart, tyStart))
            if (dir.x() > 0)
                normal = Vec2f.LEFT;
            else
                normal = Vec2f.RIGHT;
        else
        if (dir.y() > 0)
            normal = Vec2f.DOWN;
        else
            normal = Vec2f.UP;

        return Optional.of(new IntersectionData(tStart, normal));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        return (obj instanceof Segment(Vec2f o2, Vec2f d2))
                && origin.equals(o2)
                && destination.equals(d2);
    }

    public static Segment fromDirection(Vec2f origin, Vec2f direction) {
        if (direction.equals(Vec2f.ZERO))
            throw new IllegalArgumentException("The direction of a segment can't be the zero vector");

        return new Segment(origin, origin.add(direction));
    }
}