package com.game.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IntersectionUtilsTest {
    @Test
    public void resolveDynamicIntersectionTest() {
        Vec2f v1 = new Vec2f(2, 0);
        Vec2f n1 = Vec2f.LEFT;
        float t1 = 0.5f;
        Assertions.assertEquals(
                new Vec2f(1, 0),
                IntersectionUtils.resolveDynamicIntersection(v1, new IntersectionData(t1, n1))
        );

        Vec2f v2 = new Vec2f(2, 2);
        Vec2f n2 = Vec2f.LEFT;
        float t2 = 0.5f;
        Assertions.assertEquals(
                new Vec2f(1, 2),
                IntersectionUtils.resolveDynamicIntersection(v2, new IntersectionData(t2, n2))
        );

        Vec2f v3 = new Vec2f(0, 4);
        Vec2f n3 = Vec2f.DOWN;
        float t3 = 0.25f;
        Assertions.assertEquals(
                new Vec2f(0, 1),
                IntersectionUtils.resolveDynamicIntersection(v3, new IntersectionData(t3, n3))
        );

        Vec2f v4 = new Vec2f(2, 2);
        Vec2f n4 = Vec2f.LEFT;
        float t4 = 1;
        Assertions.assertEquals(
                v4,
                IntersectionUtils.resolveDynamicIntersection(v4, new IntersectionData(t4, n4))
        );

        Vec2f v5 = new Vec2f(2, 2);
        Vec2f n5 = Vec2f.LEFT;
        float t5 = 0;
        Assertions.assertEquals(
                new Vec2f(0, 2),
                IntersectionUtils.resolveDynamicIntersection(v5, new IntersectionData(t5, n5))
        );

        Vec2f v6 = new Vec2f(2, 2);
        Vec2f n6 = Vec2f.ZERO;
        float t6 = 0.5f;
        Assertions.assertEquals(
                v6,
                IntersectionUtils.resolveDynamicIntersection(v6, new IntersectionData(t6, n6))
        );

        Vec2f v7 = Vec2f.ZERO;
        Vec2f n7 = Vec2f.UP;
        float t7 = 0.7f;
        Assertions.assertEquals(
                Vec2f.ZERO,
                IntersectionUtils.resolveDynamicIntersection(v7, new IntersectionData(t7, n7))
        );
    }
}
