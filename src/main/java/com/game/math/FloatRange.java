package com.game.math;

import java.util.Optional;

public record FloatRange(float min, float max) {
    public FloatRange {
        if (min > max) throw new IllegalArgumentException("The minimum of a range can't be higher than the maximum");
    }

    public Optional<FloatRange> intersection(FloatRange other) {
        if (this.min > other.max) return Optional.empty();
        if (other.min > this.max) return Optional.empty();

        float newMin = Math.max(this.min, other.min);
        float newMax = Math.min(this.max, other.max);
        return Optional.of(new FloatRange(newMin, newMax));
    }

    public static final FloatRange ZERO_ONE = new FloatRange(0, 1);
}
