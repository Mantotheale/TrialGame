package com.game.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Vec2fTest {
    @Test
    public void equalsTest() {
        Vec2f a = new Vec2f(1, 2);
        Assertions.assertNotEquals(null, a);
        Assertions.assertEquals(a, a);

        Vec2f b = new Vec2f(1, 2);
        Assertions.assertEquals(a, b);

        b = new Vec2f(3, 4);
        Assertions.assertNotEquals(a, b);

        b = new Vec2f(2, 2);
        Assertions.assertNotEquals(a, b);

        b = new Vec2f(1, 7);
        Assertions.assertNotEquals(a, b);
    }

    @Test
    public void zeroTest() {
        Assertions.assertEquals(new Vec2f(0, 0), Vec2f.ZERO);
        Assertions.assertEquals(Vec2f.ZERO, Vec2f.ZERO.add(Vec2f.ZERO));
        Assertions.assertEquals(Vec2f.ZERO, Vec2f.ZERO.sub(Vec2f.ZERO));
    }

    @Test
    public void basicAddTest() {
        Vec2f a = new Vec2f(1, 2);
        Vec2f b = new Vec2f(0.5f, 1.5f);
        Assertions.assertEquals(new Vec2f(1.5f, 3.5f), a.add(b));

        a = new Vec2f(-7.5f, 10);
        b = new Vec2f(7.5f, 9.5f);
        Assertions.assertEquals(new Vec2f(0, 19.5f), a.add(b));
    }

    @Test
    public void addZeroTest() {
        Vec2f a = new Vec2f(1, 2);

        Assertions.assertEquals(a, a.add(Vec2f.ZERO));
        Assertions.assertEquals(a, Vec2f.ZERO.add(a));
    }

    @Test
    public void commutativeTest() {
        Vec2f a = new Vec2f(8, -3.7f);
        Vec2f b = new Vec2f(-2, 40.f);

        Assertions.assertEquals(a.add(b), b.add(a));
    }

    @Test
    public void basicSubTest() {
        Vec2f a = new Vec2f(1, 2);
        Vec2f b = new Vec2f(0.5f, 1.5f);
        Assertions.assertEquals(new Vec2f(0.5f, 0.5f), a.sub(b));

        a = new Vec2f(-7.5f, 10);
        b = new Vec2f(7.5f, 9.5f);
        Assertions.assertEquals(new Vec2f(-15, 0.5f), a.sub(b));
    }

    @Test
    public void subZeroTest() {
        Vec2f a = new Vec2f(1, 2);

        Assertions.assertEquals(a, a.sub(Vec2f.ZERO));
        Assertions.assertEquals(new Vec2f(-1, -2), Vec2f.ZERO.sub(a));
    }

    @Test
    public void negateTest() {
        Vec2f v = new Vec2f(1, 2);
        Assertions.assertEquals(new Vec2f(-1, -2), v.negate());

        v = Vec2f.ZERO;
        Assertions.assertEquals(Vec2f.ZERO, v.negate());
    }

    @Test
    public void squaredLenTest() {
        Assertions.assertTrue(FloatUtils.eq(25, new Vec2f(3, 4).squaredLen()));
        Assertions.assertTrue(FloatUtils.eq(25, new Vec2f(0, 5).squaredLen()));
        Assertions.assertTrue(FloatUtils.eq(0, Vec2f.ZERO.squaredLen()));
        Assertions.assertTrue(FloatUtils.eq(25, new Vec2f(-3, -4).squaredLen()));
    }

    @Test
    public void lenTest() {
        Assertions.assertTrue(FloatUtils.eq(5, new Vec2f(3, 4).len()));
        Assertions.assertTrue(FloatUtils.eq(5, new Vec2f(0, 5).len()));
        Assertions.assertTrue(FloatUtils.eq(0, Vec2f.ZERO.len()));
        Assertions.assertTrue(FloatUtils.eq(5, new Vec2f(-3, -4).len()));
    }

    @Test
    public void normalizeTest() {
        Assertions.assertEquals(new Vec2f(0.6f, 0.8f), new Vec2f(3, 4).normalize());
        Assertions.assertEquals(Vec2f.RIGHT, Vec2f.RIGHT.normalize());
        Assertions.assertEquals(Vec2f.UP, Vec2f.UP.normalize());
        Assertions.assertEquals(new Vec2f(-0.6f, -0.8f), new Vec2f(-3, -4).normalize());

        Vec2f normalized = new Vec2f(3, 4).normalize();
        Assertions.assertTrue(FloatUtils.eq(1, normalized.len()));
    }

    @Test
    public void normalizeZeroTest() {
        Assertions.assertEquals(Vec2f.ZERO, Vec2f.ZERO.normalize());
    }

    @Test
    public void mulTest() {
        Assertions.assertEquals(new Vec2f(2, 4), new Vec2f(1, 2).mul(2));
        Assertions.assertEquals(new Vec2f(-2, -4), new Vec2f(1, 2).mul(-2));
        Assertions.assertEquals(new Vec2f(0.5f, 1), new Vec2f(1, 2).mul(0.5f));
        Assertions.assertEquals(Vec2f.ZERO, new Vec2f(1, 2).mul(0));
        Assertions.assertEquals(new Vec2f(1, 2), new Vec2f(1, 2).mul(1));
        Assertions.assertEquals(new Vec2f(-1, -2), new Vec2f(1, 2).mul(-1));
    }

    @Test
    public void dotTest() {
        Assertions.assertTrue(FloatUtils.eq(new Vec2f(1, 2).dot(new Vec2f(3, 4)), 11));
        Assertions.assertTrue(FloatUtils.eq(new Vec2f(1, 0).dot(new Vec2f(0, 1)), 0));
        Assertions.assertTrue(FloatUtils.eq(new Vec2f(1, 0).dot(new Vec2f(1, 0)), 1));
        Assertions.assertTrue(FloatUtils.eq(new Vec2f(1, 0).dot(new Vec2f(-1, 0)), -1));
        Assertions.assertTrue(FloatUtils.eq(new Vec2f(3, 4).dot(Vec2f.ZERO), 0));
        Assertions.assertTrue(FloatUtils.eq(new Vec2f(2, 3).dot(new Vec2f(4, 5)), new Vec2f(4, 5).dot(new Vec2f(2, 3))));
    }
}
