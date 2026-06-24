package com.game.math;

import java.util.Optional;

public sealed interface Shape permits Circle, Rectangle {
    Vec2f center();
    Shape moveTo(Vec2f position);
    boolean contains(Vec2f position);
    boolean staticIntersects(Shape other);
    Optional<IntersectionData> dynamicIntersection(Vec2f velocity, Shape other);
}
