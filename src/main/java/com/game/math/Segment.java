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

    public Optional<IntersectionData> intersection(Circle circle) {
        if (circle.contains(origin))
            return Optional.of(new IntersectionData(0, origin.sub(circle.center()).normalize()));

        // Transform the segment into an ax + by + c = 0 line
        // The line passing for (x1, y1) and (x2, y2) is
        // a = y2 - y1, b = -(x2 - x1), c = x2y1 - x1y2
        float a = destination.y() - origin.y();
        float b = origin.x() - destination.x();
        float c = destination.x() * origin.y() - origin.x() * destination.y();

        // The equation for the circle is (x - x_k)^2 + (y - y_k)^2 = r
        float xk = circle.center().x();
        float yk = circle.center().y();
        float r = circle.radius();


        Vec2f p1, p2;
        if (FloatUtils.eq(a, 0)) {
            // if a = 0, we obtain the system of equations
            // y = -c / b
            // x = x_k +- sqrt(r^2 - ((c + by_k) / b)^2)
            float invB = 1 / b;
            float y = -c * invB;
            float inner = (c + b * yk) * invB;
            float D = r * r - inner * inner;
            if (FloatUtils.lt(D, 0)) return Optional.empty();

            float sqrtD = FloatUtils.eq(D, 0) ? 0 : (float) Math.sqrt(D);
            float x1 = xk + sqrtD;
            float x2 = xk - sqrtD;
            p1 = new Vec2f(x1, y);
            p2 = new Vec2f(x2, y);
        } else {
            // By solving a system of equations, if a != 0, we get
            // (a^2 + b^2)y^2 + (2bc + 2abx_k - 2a^2y_k)y + (c^2 + 2acx_k + a^2x_k^2 + a^2y_k^2 - a^2r^2) = 0
            // x = (-by - c) / a
            // We call
            // A = a^2 + b^2
            // B = 2bc + 2abx_k - 2a^2y_k
            // C = c^2 + 2acx_k + a^2x_k^2 + a^2y_k^2 - a^2r^2
            // And we solve Ay^2 + By + C = 0, by finding the discriminant

            float aSqr = a * a;
            float bSqr = b * b;
            float cSqr = c * c;
            float ab = a * b;
            float ac = a * c;
            float bc = b * c;

            float A = aSqr + bSqr;
            float B = 2 * (bc + ab * xk - aSqr * yk);
            float C = cSqr + 2 * ac * xk + aSqr * (xk * xk + yk * yk - r * r);

            float D = B * B - 4 * A * C;
            if (FloatUtils.lt(D, 0)) return Optional.empty();

            float sqrtD = FloatUtils.eq(D, 0) ? 0 : (float) Math.sqrt(D);
            float inv2A = 0.5f * (1 / A);
            float y1 = (-B + sqrtD) * inv2A;
            float y2 = (-B - sqrtD) * inv2A;

            float aInv = 1 / a;
            float x1 = -(b * y1 + c) * aInv;
            float x2 = -(b * y2 + c) * aInv;

            p1 = new Vec2f(x1, y1);
            p2 = new Vec2f(x2, y2);
        }

        // After getting the intersection point, we check if it is inside the segment.
        // Let's call the origin O, destination D and intersection I
        // If 0 <= (OI / OD) <= 1, then it's part of it, and the result is the "time" of intersection

        float t1, t2;
        Vec2f dir = direction();
        if (FloatUtils.eq(dir.x(), 0)) {
            float invDy = 1 / dir.y();
            t1 = (p1.y() -  origin.y()) * invDy;
            t2 = (p2.y() -  origin.y()) * invDy;
        } else {
            float invDx = 1 / dir.x();
            t1 = (p1.x() -  origin.x()) * invDx;
            t2 = (p2.x() -  origin.x()) * invDx;
        }

        PointTime pt1 = new PointTime(p1, t1);
        PointTime pt2 = new PointTime(p2, t2);
        Optional<PointTime> optMin = PointTime.min(pt1, pt2);

        if (optMin.isEmpty()) return Optional.empty();
        PointTime pt = optMin.get();
        Vec2f normal = new Vec2f(pt.point.x() - xk, pt.point.y() - yk).normalize();

        return Optional.of(new IntersectionData(pt.t, normal));
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

    private record PointTime(Vec2f point, float t) {
        public boolean isValid() {
            return FloatUtils.geq(t, 0) && FloatUtils.leq(t, 1);
        }

        public static Optional<PointTime> min(PointTime p1, PointTime p2) {
            if (!p1.isValid() && !p2.isValid()) return Optional.empty();
            if (!p1.isValid()) return Optional.of(p2);
            if (!p2.isValid()) return Optional.of(p1);
            if (p1.t < p2.t) return Optional.of(p1);
            return Optional.of(p2);
        }
    }
}