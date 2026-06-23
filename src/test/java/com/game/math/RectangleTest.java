package com.game.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class RectangleTest {
    @Test
    public void equalsTest() {
        Vec2f c = new Vec2f(5, 7);
        float w = 4;
        float h = 1;
        Rectangle rect = new Rectangle(c, w, h);

        Assertions.assertEquals(rect, new Rectangle(c, w, h));
    }

    @Test
    public void validConstructionTest() {
        Vec2f c1 = new Vec2f(-1, 0);
        float w1 = -4;
        float h1 = 100;
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Rectangle(c1, w1, h1));

        Vec2f c2 = new Vec2f(-1, 9);
        float w2 = 5;
        float h2 = -100;
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Rectangle(c2, w2, h2));
    }

    @Test
    public void sidesTest() {
        Rectangle r = new Rectangle(new Vec2f(1, 2), 3, 4);
        Assertions.assertTrue(FloatUtils.eq(-0.5f, r.left()));
        Assertions.assertTrue(FloatUtils.eq(2.5f, r.right()));
        Assertions.assertTrue(FloatUtils.eq(0, r.bottom()));
        Assertions.assertTrue(FloatUtils.eq(4, r.top()));

        r = new Rectangle(new Vec2f(-5, -1), 10, 6);
        Assertions.assertTrue(FloatUtils.eq(-10, r.left()));
        Assertions.assertTrue(FloatUtils.eq(0, r.right()));
        Assertions.assertTrue(FloatUtils.eq(-4, r.bottom()));
        Assertions.assertTrue(FloatUtils.eq(2, r.top()));
    }

    @Test
    public void basicRectDynamicIntersectionTest() {
        Rectangle stationary = new Rectangle(new Vec2f(3, 0), 1, 1);

        Rectangle moving = new Rectangle(new Vec2f(0, 0), 1, 1);
        Vec2f velocity = new Vec2f(5, 0);
        Assertions.assertEquals(
                Optional.of(new IntersectionData(2f / 5f, Vec2f.LEFT)),
                moving.dynamicIntersection(velocity, stationary)
        );

        moving = new Rectangle(new Vec2f(6, 0), 1, 1);
        velocity = new Vec2f(-5, 0);
        Assertions.assertEquals(
                Optional.of(new IntersectionData(2f / 5f, Vec2f.RIGHT)),
                moving.dynamicIntersection(velocity, stationary)
        );

        moving = new Rectangle(new Vec2f(3, -3), 1, 1);
        velocity = new Vec2f(0, 6);
        Assertions.assertEquals(
                Optional.of(new IntersectionData(2f / 6f, Vec2f.DOWN)),
                moving.dynamicIntersection(velocity, stationary)
        );

        moving = new Rectangle(new Vec2f(3, 4), 1, 1);
        velocity = new Vec2f(0, -6);
        Assertions.assertEquals(
                Optional.of(new IntersectionData(3f / 6f, Vec2f.UP)),
                moving.dynamicIntersection(velocity, stationary)
        );
    }

    @Test
    public void noRectDynamicIntersectionTest() {
        Rectangle moving = new Rectangle(new Vec2f(0, 0), 1, 1);
        Rectangle stationary = new Rectangle(new Vec2f(3, 0), 1, 1);

        Vec2f velocity = new Vec2f(-5, 0);
        Assertions.assertEquals(
                Optional.empty(),
                moving.dynamicIntersection(velocity, stationary)
        );

        velocity = new Vec2f(1, 0);
        Assertions.assertEquals(
                Optional.empty(),
                moving.dynamicIntersection(velocity, stationary)
        );

        velocity = new Vec2f(5, 3);
        Assertions.assertEquals(
                Optional.empty(),
                moving.dynamicIntersection(velocity, stationary)
        );
    }

    @Test
    public void cornerRectDynamicIntersectionTest() {
        Rectangle stationary = new Rectangle(new Vec2f(3, 3), 1, 1);

        Rectangle moving = new Rectangle(new Vec2f(0, 0), 1, 1);
        Vec2f velocity = new Vec2f(6, 6);
        Assertions.assertEquals(
                Optional.of(new IntersectionData(2f / 6f, Vec2f.DOWN.add(Vec2f.LEFT).normalize())),
                moving.dynamicIntersection(velocity, stationary)
        );

        moving = new Rectangle(new Vec2f(0, 4), 1, 1);
        velocity = new Vec2f(4, -4);
        Assertions.assertEquals(
                Optional.of(new IntersectionData(0.5f, Vec2f.ZERO)),
                moving.dynamicIntersection(velocity, stationary)
        );
    }

    @Test
    public void differentSizeRectDynamicIntersectionTest() {
        Rectangle moving = new Rectangle(new Vec2f(0, 0), 2, 2);
        Rectangle stationary = new Rectangle(new Vec2f(4, 0), 1, 1);
        Vec2f velocity = new Vec2f(6, 0);
        Assertions.assertEquals(
                Optional.of(new IntersectionData(2.5f / 6f, Vec2f.LEFT)),
                moving.dynamicIntersection(velocity, stationary)
        );

        moving = new Rectangle(new Vec2f(0, 0), 1, 2);
        stationary = new Rectangle(new Vec2f(0, 5), 3, 1);
        velocity = new Vec2f(0, 8);
        Assertions.assertEquals(
                Optional.of(new IntersectionData(3.5f / 8f, Vec2f.DOWN)),
                moving.dynamicIntersection(velocity, stationary)
        );
    }

    @Test
    public void zeroVelocityRectDynamicIntersectionTest() {
        Rectangle moving = new Rectangle(new Vec2f(0, 0), 1, 1);

        Rectangle stationary = new Rectangle(new Vec2f(3, 0), 1, 1);
        Assertions.assertEquals(
                Optional.empty(),
                moving.dynamicIntersection(Vec2f.ZERO, stationary)
        );

        stationary = new Rectangle(new Vec2f(1, 0), 1, 1);
        Assertions.assertEquals(
                Optional.of(new IntersectionData(0, Vec2f.ZERO)),
                moving.dynamicIntersection(Vec2f.ZERO, stationary)
        );
    }

    @Test
    public void staticIntersectsTest() {
        Rectangle rect = new Rectangle(new Vec2f(0, 0), 2, 2);

        Rectangle other = new Rectangle(new Vec2f(1, 0), 2, 2);
        Assertions.assertTrue(rect.staticIntersects(other));
        other = new Rectangle(new Vec2f(0, 1), 2, 2);
        Assertions.assertTrue(rect.staticIntersects(other));
        other = new Rectangle(new Vec2f(0, 0), 1, 1);
        Assertions.assertTrue(rect.staticIntersects(other));

        Assertions.assertTrue(rect.staticIntersects(rect));

        other = new Rectangle(new Vec2f(2, 0), 2, 2);
        Assertions.assertTrue(rect.staticIntersects(other));
        other = new Rectangle(new Vec2f(-2, 0), 2, 2);
        Assertions.assertTrue(rect.staticIntersects(other));
        other = new Rectangle(new Vec2f(0, 2), 2, 2);
        Assertions.assertTrue(rect.staticIntersects(other));
        other = new Rectangle(new Vec2f(0, -2), 2, 2);
        Assertions.assertTrue(rect.staticIntersects(other));

        other = new Rectangle(new Vec2f(3, 0), 2, 2);
        Assertions.assertFalse(rect.staticIntersects(other));
        other = new Rectangle(new Vec2f(-3, 0), 2, 2);
        Assertions.assertFalse(rect.staticIntersects(other));
        other = new Rectangle(new Vec2f(0, 3), 2, 2);
        Assertions.assertFalse(rect.staticIntersects(other));
        other = new Rectangle(new Vec2f(0, -3), 2, 2);
        Assertions.assertFalse(rect.staticIntersects(other));

        other = new Rectangle(new Vec2f(3, 3), 2, 2);
        Assertions.assertFalse(rect.staticIntersects(other));
        other = new Rectangle(new Vec2f(-3, -3), 2, 2);
        Assertions.assertFalse(rect.staticIntersects(other));
    }
}
