package com.game.math;

public class IntersectionUtils {
    private IntersectionUtils() { }

    public static Vec2f resolveDynamicIntersection(Vec2f velocity, IntersectionData intersectionData) {
        /*if (velocity.equals(Vec2f.ZERO)) return Vec2f.ZERO;

        float t = intersectionData.t();
        Vec2f normal = intersectionData.normal();

        // Vf = V - N * dot(V * (1 -t), N)
        Vec2f remainderVelocity = velocity.mul(1 - t);
        float dot = remainderVelocity.dot(normal);

        if (FloatUtils.geq(dot, 0)) return velocity;
        Vec2f scaledNormal = normal.mul(dot);

        return velocity.sub(scaledNormal);*/
        if (velocity.equals(Vec2f.ZERO)) return Vec2f.ZERO;

        Vec2f normal = intersectionData.normal();
        float dot = velocity.dot(normal);

        if (FloatUtils.geq(dot, 0)) return velocity;

        return velocity.sub(normal.mul(dot));
    }
}
