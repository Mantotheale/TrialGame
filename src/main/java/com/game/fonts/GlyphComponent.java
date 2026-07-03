package com.game.fonts;

import org.jetbrains.annotations.NotNull;

record GlyphComponent(
        short glyphIdx,
        GlyphComponentArguments arguments,
        GlyphTransformation transformation,
        boolean useComponentMetrics
) {
    @Override
    public @NotNull String toString() {
        return "GlyphComponent(" +
                "glyphIdx: " + Short.toUnsignedInt(glyphIdx) + ", " +
                "arguments: " + arguments + ", " +
                "transformation: " + transformation + ", " +
                "useComponentMetrics: " + useComponentMetrics +
                ")";
    }
}