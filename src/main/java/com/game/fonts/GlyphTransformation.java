package com.game.fonts;

import org.jetbrains.annotations.NotNull;

sealed interface GlyphTransformation {
    boolean applyToOffset();
    boolean roundToGrid();

    GlyphTransformation ROUNDED_IDENTITY = new Identity(true);
    GlyphTransformation UNROUNDED_IDENTITY = new Identity(false);

    record Identity(boolean roundToGrid) implements GlyphTransformation {
        @Override
        public @NotNull String toString() {
            return "Identity";
        }

        @Override
        public boolean applyToOffset() {
            return false;
        }
    }

    record UniformScale(float scale, boolean applyToOffset, boolean roundToGrid) implements GlyphTransformation {
        @Override
        public @NotNull String toString() {
            return "UniformScale(" + scale + ", applyToOffset: " + applyToOffset + ", roundToGrid: " + roundToGrid + ")";
        }
    }

    record UnevenScale(float xScale, float yScale, boolean applyToOffset, boolean roundToGrid) implements GlyphTransformation {
        @Override
        public @NotNull String toString() {
            return "UnevenScale(xScale: " + xScale + ", yScale: " + yScale + ", applyToOffset: " + applyToOffset + ", roundToGrid: " + roundToGrid + ")";
        }
    }

    record MatrixScale(float s00, float s01, float s10, float s11, boolean applyToOffset, boolean roundToGrid) implements GlyphTransformation {
        @Override
        public @NotNull String toString() {
            return "MatrixScale(s00: " + s00 + ", s01: " + s01 + ", s10: " + s10 + ", s11: " + s11 + ", applyToOffset: " + applyToOffset + ", roundToGrid: " + roundToGrid + ")";
        }
    }
}
