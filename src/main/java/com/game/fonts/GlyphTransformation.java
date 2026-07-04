package com.game.fonts;

import com.game.math.Vec2f;
import org.jetbrains.annotations.NotNull;

sealed interface GlyphTransformation {
    boolean applyToOffset();
    boolean roundToGrid();
    Vec2f transformPoint(Vec2f point);

    GlyphTransformation ROUNDED_IDENTITY = new Identity(true);
    GlyphTransformation UNROUNDED_IDENTITY = new Identity(false);

    record Identity(boolean roundToGrid) implements GlyphTransformation {
        @Override
        public @NotNull String toString() {
            return "Identity";
        }

        @Override
        public boolean applyToOffset() {
            return true;
        }

        @Override
        public Vec2f transformPoint(Vec2f point) {
            return point;
        }
    }

    record UniformScale(float scale, boolean applyToOffset, boolean roundToGrid) implements GlyphTransformation {
        @Override
        public @NotNull String toString() {
            return "UniformScale(" + scale + ", applyToOffset: " + applyToOffset + ", roundToGrid: " + roundToGrid + ")";
        }

        @Override
        public Vec2f transformPoint(Vec2f point) {
            return point.scale(scale);
        }
    }

    record UnevenScale(float xScale, float yScale, boolean applyToOffset, boolean roundToGrid) implements GlyphTransformation {
        @Override
        public @NotNull String toString() {
            return "UnevenScale(xScale: " + xScale + ", yScale: " + yScale + ", applyToOffset: " + applyToOffset + ", roundToGrid: " + roundToGrid + ")";
        }

        @Override
        public Vec2f transformPoint(Vec2f point) {
            return point.scale(new Vec2f(xScale, yScale));
        }
    }

    record MatrixScale(float a, float b, float c, float d, boolean applyToOffset, boolean roundToGrid) implements GlyphTransformation {
        @Override
        public @NotNull String toString() {
            return "MatrixScale(a: " + a + ", b: " + b + ", c: " + c + ", d: " + d + ", applyToOffset: " + applyToOffset + ", roundToGrid: " + roundToGrid + ")";
        }

        @Override
        public Vec2f transformPoint(Vec2f point) {
            return new Vec2f(a * point.x() + c * point.y(), b * point.x() + d * point.y());
        }
    }
}
