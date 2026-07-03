package com.game.fonts;

import org.jetbrains.annotations.NotNull;

public record FontPoint(short x, short y, boolean onCurve) {
    @Override
    public @NotNull String toString() {
        return "FontPoint(x: " + x + ", y: " + y + ", onCurve: " + onCurve + ")";
    }
}
