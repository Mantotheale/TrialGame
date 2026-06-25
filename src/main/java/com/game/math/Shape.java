package com.game.math;

import java.util.Optional;

public sealed interface Shape permits Circle, Rectangle {
    Vec2f center();
    Shape moveTo(Vec2f position);
    boolean contains(Vec2f position);
    boolean staticIntersects(Shape other);
    Optional<IntersectionData> dynamicIntersection(Vec2f velocity, Shape other);

    static boolean staticRectCircleIntersects(Rectangle rect, Circle circle) {
        Vec2f rectCenter = rect.center();
        Vec2f circleCenter = circle.center();
        float halfRectWidth = 0.5f * rect.width();
        float halfRectHeight = 0.5f * rect.height();
        float circleRadius = circle.radius();

        float dx = Math.abs(rectCenter.x() - circleCenter.x());
        float dy = Math.abs(rectCenter.y() - circleCenter.y());

        if (FloatUtils.gt(dx, halfRectWidth + circleRadius)) return false;
        if (FloatUtils.gt(dy, halfRectHeight + circleRadius)) return false;

        if (FloatUtils.leq(dx, halfRectWidth)) return true;
        if (FloatUtils.leq(dy, halfRectHeight)) return true;

        float leftoverX = dx - halfRectWidth;
        float leftoverY = dy - halfRectHeight;
        return leftoverX * leftoverX + leftoverX * leftoverY <= circleRadius * circleRadius;
    }
}
