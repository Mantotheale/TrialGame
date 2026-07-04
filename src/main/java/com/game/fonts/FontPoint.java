package com.game.fonts;

import com.game.math.Vec2f;
import org.jetbrains.annotations.NotNull;

public record FontPoint(short x, short y, boolean onCurve) {
    FontPoint applyTransformation(GlyphTransformation transformation) {
        Vec2f transformed = transformation.transformPoint(new Vec2f(x, y));
        return new FontPoint((short) transformed.x(), (short) transformed.y(), onCurve);
    }

    FontPoint applyOffset(Vec2f offset) {
        return new FontPoint((short) (x + offset.x()), (short) (y + offset.y()), onCurve);
    }

    @Override
    public @NotNull String toString() {
        return "FontPoint(x: " + x + ", y: " + y + ", onCurve: " + onCurve + ")";
    }
}
