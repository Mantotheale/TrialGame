package com.game.collision;

import com.game.math.IntersectionData;
import com.game.math.IntersectionUtils;
import com.game.math.Rectangle;
import com.game.math.Vec2f;

public record Collider(Rectangle rect, boolean isFixed) {
    public Vec2f position() {
        return rect.center();
    }

    public Collider moveTo(Vec2f position) {
        return new Collider(new Rectangle(position, rect.width(), rect.height()), isFixed);
    }

    public Vec2f resolveCollision(Vec2f initialVelocity, IntersectionData intersectionData) {
        return IntersectionUtils.resolveDynamicIntersection(initialVelocity, intersectionData);
    }
}
