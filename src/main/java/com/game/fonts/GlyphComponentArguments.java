package com.game.fonts;

import org.jetbrains.annotations.NotNull;

sealed interface GlyphComponentArguments {
    record Offset(short x, short y) implements  GlyphComponentArguments {
        @Override
        public @NotNull String toString() {
            return "Offset(x: " + x + ", y: " + y + ")";
        }
    }

    record Points(short parentPointId, short childPointId) implements  GlyphComponentArguments {
        @Override
        public @NotNull String toString() {
            return "Points(parentPointId: "
                    + Short.toUnsignedInt(parentPointId) + ", " +
                    "childPointId: " + Short.toUnsignedInt(childPointId)
                    + ")";
        }
    }
}
