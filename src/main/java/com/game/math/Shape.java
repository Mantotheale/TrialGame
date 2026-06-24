package com.game.math;

import java.util.Optional;

public sealed interface Shape permits Rectangle {
    Vec2f center();
    Shape moveTo(Vec2f position);
    Optional<IntersectionData> dynamicIntersection(Vec2f velocity, Shape other);
}
