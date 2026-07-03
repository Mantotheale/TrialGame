package com.game.math;

import java.util.ArrayList;
import java.util.List;

public record BezierCurveOrder2(Vec2f origin, Vec2f control, Vec2f destination) {
    public Vec2f evaluate(float t) {
        if (FloatUtils.lt(t, 0) || FloatUtils.gt(t, 1))
            throw new IllegalArgumentException("Bezier curves are only defined for t in [0, 1]. t was " + t);

        // B(t) = (p0 + p2 - 2p1)t^2 + 2(p1 - p0)t + p0
        Vec2f a = origin.add(destination).sub(control.scale(2));
        Vec2f b = control.sub(origin).scale(2);
        return a.scale(t * t).add(b.scale(t)).add(origin);
    }

    public List<Segment> linearize(int parts) {
        float step = 1f /  parts;

        List<Vec2f> points = new ArrayList<>();
        for (int i = 0; i <= parts; i++)
            points.add(evaluate(i * step));

        List<Segment> segments = new ArrayList<>();
        for (int i = 1; i <= parts; i++)
            segments.add(new Segment(points.get(i - 1), points.get(i)));

        return segments;
    }
}
