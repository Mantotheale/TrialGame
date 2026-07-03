package com.game.fonts;

import org.jetbrains.annotations.NotNull;

record FontPoint(short x, short y, boolean onCurve) {
    @Override
    public @NotNull String toString() {
        return "FontCoord(x: " + x + ", y: " + y + ", onCurve: " + onCurve + ")";
    }
}
