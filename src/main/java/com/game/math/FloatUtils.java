package com.game.math;

public class FloatUtils {
    private FloatUtils() { }

    public static final float EPSILON = 1e-5F;

    public static boolean areEqualsEps(float a, float b) {
        return Math.abs(a - b) < EPSILON;
    }

    public static float avg(float... values) {
        if (values.length == 0) throw new IllegalArgumentException("Can't take the average of 0 values");

        float acc = 0;
        for (float v: values)
            acc += v;
        return acc / values.length;
    }

    public static float max(float... values) {
        if (values.length == 0) throw new IllegalArgumentException("Can't take the max of 0 values");

        float max = Float.MIN_VALUE;
        for (float v: values)
            if (v > max)
                max = v;

        return max;
    }
}
