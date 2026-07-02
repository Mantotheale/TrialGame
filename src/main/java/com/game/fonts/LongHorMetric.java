package com.game.fonts;

import org.jetbrains.annotations.NotNull;

public record LongHorMetric(short advanceWidth, short leftSideBearing) {
    @Override
    public @NotNull String toString() {
        return "LongHorMetric(" +
                "advanceWidth: " + Short.toUnsignedInt(advanceWidth) + ", " +
                "leftSideBearing: " + leftSideBearing + ", " +
                ")";
    }
}
