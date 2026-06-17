package com.game.collision;

import com.game.util.Vec2f;

public class ColliderUtils {
    private ColliderUtils() { }

    public static boolean intersect(RectangleCollider rect, CircleCollider circ) {
        Vec2f rectClosePoint = rectClosePoint(rect, circ);
        return rectClosePoint.squaredDistance(circ.center()) <= circ.radius() * circ.radius();
    }

    public static Vec2f rectClosePoint(RectangleCollider rect, CircleCollider circ) {
        float centerX = circ.center().x();
        float centerY = circ.center().y();

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

}
