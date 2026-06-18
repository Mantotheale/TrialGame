package com.game.collision;

import com.game.util.FloatUtils;
import com.game.util.Vec2f;

import java.util.ArrayList;
import java.util.List;

public class ColliderUtils {
    private ColliderUtils() { }

    public static boolean intersect(RectangleCollider rect, CircleCollider circ) {
        Vec2f rectClosePoint = closestPointInRect(rect, circ.center());
        return rectClosePoint.squaredDistance(circ.center()) <= circ.radius() * circ.radius();
    }

    public static Vec2f closestPointInRect(RectangleCollider rect, Vec2f point) {
        float centerX = point.x();
        float centerY = point.y();

        float rectClosePointX = centerX;
        if (centerX < rect.left())
            rectClosePointX = rect.left();
        else if (centerX > rect.right())
            rectClosePointX = rect.right();

        float rectClosePointY = centerY;
        if (centerY < rect.bottom())
            rectClosePointY = rect.bottom();
        else if (centerY > rect.top())
            rectClosePointY = rect.top();

        return new Vec2f(rectClosePointX, rectClosePointY);
    }

    public static RectOutProjection projectToRectPerimeter(RectangleCollider rect, Vec2f point) {


        float closestX, closestY;
        Vec2f outDirectionX, outDirectionY;

        if (point.x() >= rect.center().x()) {
            closestX = rect.right();
            outDirectionX = Vec2f.RIGHT;
        } else {
            closestX = rect.left();
            outDirectionX = Vec2f.LEFT;
        }

        if (point.y() >= rect.center().y()) {
            closestY = rect.top();
            outDirectionY = Vec2f.UP;
        } else {
            closestY = rect.bottom();
            outDirectionY = Vec2f.DOWN;
        }

        float dx = Math.abs(point.x() - closestX);
        float dy = Math.abs(point.y() - closestY);

        Vec2f intersection, outDirection;
        if (FloatUtils.areEqualsEps(dx, dy)) {
            intersection = new Vec2f(closestX, closestY);
            outDirection = outDirectionX.add(outDirectionY).normalize();
        } else if (dx < dy) {
            intersection = new Vec2f(closestX, point.y());
            outDirection = outDirectionX;
        } else {
            intersection = new Vec2f(point.x(), closestY);
            outDirection = outDirectionY;
        }

        return new RectOutProjection(intersection, outDirection);
    }

    public static List<Vec2f> lineToRectIntersections(Vec2f p1, Vec2f p2, RectangleCollider rect) {
        if (p1.equals(p2)) throw new IllegalArgumentException("The points can't be the same");

        if (p1.x() < rect.left() && p2.x() < rect.left()) return List.of();
        if (p1.x() > rect.right() && p2.x() > rect.right()) return List.of();
        if (p1.y() < rect.bottom() && p2.y() < rect.bottom()) return List.of();
        if (p1.y() > rect.top() && p2.y() > rect.top()) return List.of();

        float a = p1.y() - p2.y();
        float b = p2.x() - p1.x();
        float c = p1.x() * p2.y() - p2.x() * p1.y();

        Vec2f pLeft = p1.x() < p2.x() ? p1 : p2;
        Vec2f pRight = p1.x() < p2.x() ? p2 : p1;
        Vec2f pDown = p1.y() < p2.y() ? p1 : p2;
        Vec2f pUp = p1.y() < p2.y() ? p2 : p1;

        List<Vec2f> points = new ArrayList<>();
        if (FloatUtils.areEqualsEps(a, 0)) {
            if (pLeft.x() < rect.left()) points.add(new Vec2f(rect.left(), pLeft.y()));
            if (pRight.x() > rect.right()) points.add(new Vec2f(rect.right(), pLeft.y()));

            if (FloatUtils.areEqualsEps(p1.y(), rect.top()) || FloatUtils.areEqualsEps(p1.y(), rect.bottom()))
                if (points.isEmpty()) points.add(Vec2f.middlePoint(pLeft, pRight));
        } else if (FloatUtils.areEqualsEps(b, 0)) {
            if (pDown.y() < rect.bottom()) points.add(new Vec2f(pDown.x(), rect.bottom()));
            if (pUp.y() > rect.top()) points.add(new Vec2f(pUp.x(), rect.top()));

            if (FloatUtils.areEqualsEps(p1.x(), rect.left()) || FloatUtils.areEqualsEps(p1.x(), rect.right()))
                if (points.isEmpty()) points.add(Vec2f.middlePoint(pDown, pUp));
        } else {
            float topIntersectionX = (-b * rect.top() - c) / a;
            if (topIntersectionX >= pLeft.x() && topIntersectionX <= pRight.x())
                points.add(new Vec2f(topIntersectionX, rect.top()));

            float bottomIntersectionX = (-b * rect.bottom() - c) / a;
            if (bottomIntersectionX >= pLeft.x() && bottomIntersectionX <= pRight.x())
                points.add(new Vec2f(bottomIntersectionX, rect.top()));

            float leftIntersectionY = (-a * rect.left() - c) / b;
            if (leftIntersectionY >= pDown.y() && leftIntersectionY <= pUp.y())
                points.add(new Vec2f(rect.left(), leftIntersectionY));

            float rightIntersectionY = (-a * rect.right() - c) / b;
            if (rightIntersectionY >= pDown.y() && rightIntersectionY <= pUp.y())
                points.add(new Vec2f(rect.right(), rightIntersectionY));
        }

        return points;
    }

    public record RectOutProjection(Vec2f intersection, Vec2f outDirection) { }
}
