package com.game.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class SegmentTest {
    @Test
    public void equalsTest() {
        Vec2f origin = new Vec2f(0, 3);
        Vec2f destination = new Vec2f(7, 8);
        Segment r = new Segment(origin, destination);

        Assertions.assertEquals(new Segment(origin, destination), r);
        Assertions.assertNotEquals(null, r);
    }

    @Test
    public void validConstructionTest() {
        Vec2f p = new Vec2f(1, 3);
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Segment(p, p));
    }

    @Test
    public void directionTest() {
        Vec2f origin = new Vec2f(0, 3);
        Vec2f destination = new Vec2f(7, 8);
        Segment r = new Segment(origin, destination);
        Assertions.assertEquals(new Vec2f(7, 5), r.direction());

        origin = new Vec2f(-1, 2);
        destination = new Vec2f(-5, -4);
        r = new Segment(origin, destination);
        Assertions.assertEquals(new Vec2f(-4, -6), r.direction());
        Assertions.assertNotEquals(Vec2f.ZERO, r.direction());
    }

    @Test
    public void basicRectIntersectionTest() {
        Segment segment = new Segment(new Vec2f(0, 0), new Vec2f(3, 3));
        Rectangle rect = new Rectangle(new Vec2f(2, 1), 1, 1);
        Assertions.assertEquals(
                Optional.of(new IntersectionData(0.5f, Vec2f.ZERO)),
                segment.intersection(rect)
        );

        segment = new Segment(new Vec2f(1, 5), new Vec2f(3, -3));
        rect = new Rectangle(new Vec2f(2, 1), 1, 1);
        Assertions.assertEquals(
                Optional.of(new IntersectionData(3.60772f / 8.24621f, Vec2f.UP)),
                segment.intersection(rect)
        );

        segment = new Segment(new Vec2f(0, 4), new Vec2f(7, 3));
        rect = new Rectangle(new Vec2f(2, 1), 1, 1);
        Assertions.assertEquals(Optional.empty(), segment.intersection(rect));

        segment = new Segment(new Vec2f(-1, 2), new Vec2f(6, 1));
        rect = new Rectangle(new Vec2f(2, 1), 1, 1);
        Assertions.assertEquals(
                Optional.of(new IntersectionData(3.53553f / 7.07107f, Vec2f.ZERO)),
                segment.intersection(rect)
        );

        segment = new Segment(new Vec2f(7, 2), new Vec2f(-3, 1));
        rect = new Rectangle(new Vec2f(2, 1), 1, 1);
        Assertions.assertEquals(
                Optional.of(new IntersectionData(5.02494f / 10.04988f, Vec2f.UP)),
                segment.intersection(rect)
        );
    }

    @Test
    public void parallelRectIntersectionTest() {
        Segment segment = new Segment(new Vec2f(0, -5), new Vec2f(0, 7));

        Rectangle rect = new Rectangle(new Vec2f(1, 1), 2, 4);
        Assertions.assertEquals(
                Optional.of(new IntersectionData(4f / 12f, Vec2f.DOWN)),
                segment.intersection(rect)
        );
        rect = new Rectangle(new Vec2f(4, 4), 2, 2);
        Assertions.assertEquals(Optional.empty(), segment.intersection(rect));

        segment = new Segment(new Vec2f(-2, 4), new Vec2f(10, 4));

        rect = new Rectangle(new Vec2f(0, 0), 1, 10);
        Assertions.assertEquals(
                Optional.of(new IntersectionData(1.5f / 12f, Vec2f.LEFT)),
                segment.intersection(rect)
        );
        rect = new Rectangle(new Vec2f(2, -5), 4, 5);
        Assertions.assertEquals(Optional.empty(), segment.intersection(rect));
    }

    @Test
    public void cornerRectIntersectionTest() {
        Rectangle rect = new Rectangle(Vec2f.ZERO, 2, 2);

        Segment segment = new Segment(new Vec2f(0, 2), new Vec2f(2, 0));
        Assertions.assertEquals(
                Optional.of(new IntersectionData(0.5f, Vec2f.ZERO)),
                segment.intersection(rect)
        );

        segment = new Segment(new Vec2f(2, 0), new Vec2f(0, 2));
        Assertions.assertEquals(
                Optional.of(new IntersectionData(0.5f, Vec2f.ZERO)),
                segment.intersection(rect)
        );

        segment = new Segment(new Vec2f(3, 0), new Vec2f(0, 3));
        Assertions.assertEquals(Optional.empty(), segment.intersection(rect));

        segment = new Segment(new Vec2f(-3, -3), new Vec2f(0, 0));
        Assertions.assertEquals(
                Optional.of(new IntersectionData(
                        2.82843f / 4.24264f,
                        Vec2f.DOWN.add(Vec2f.LEFT).normalize()
                )),
                segment.intersection(rect)
        );
    }

    @Test
    public void sideCoincidingRectIntersectionTest() {
        Rectangle rect = new Rectangle(new Vec2f(1, 0), 2, 2);

        Segment segment = new Segment(new Vec2f(0, -2), new Vec2f(0, 4));
        Assertions.assertEquals(
                Optional.of(new IntersectionData(1f / 6f, Vec2f.DOWN)),
                segment.intersection(rect)
        );

        segment = new Segment(new Vec2f(0, 4), new Vec2f(0, 0.5f));
        Assertions.assertEquals(
                Optional.of(new IntersectionData(3 / 3.5f, Vec2f.UP)),
                segment.intersection(rect)
        );

        segment = new Segment(new Vec2f(0, -5), new Vec2f(0, -2));
        Assertions.assertEquals(Optional.empty(), segment.intersection(rect));

        segment = new Segment(new Vec2f(7, 1), new Vec2f(-12, 1));
        Assertions.assertEquals(
                Optional.of(new IntersectionData(5f / 19f, Vec2f.RIGHT)),
                segment.intersection(rect)
        );
    }

    @Test
    public void validDirectionConstructionTest() {
        Vec2f o = new Vec2f(1, 3);
        Vec2f dir = Vec2f.ZERO;
        Assertions.assertThrows(
                IllegalArgumentException.class, () -> Segment.fromDirection(o, dir)
        );
    }

    @Test
    public void directionConstructionTest() {
        Vec2f origin = new Vec2f(0, 3);
        Vec2f direction = new Vec2f(7, 5);
        Segment r = Segment.fromDirection(origin, direction);
        Assertions.assertEquals(new Vec2f(7, 8), r.destination());

        origin = new Vec2f(-1, 2);
        direction = new Vec2f(-4, -6);
        r = Segment.fromDirection(origin, direction);
        Assertions.assertEquals(new Vec2f(-5, -4), r.destination());
    }
}
