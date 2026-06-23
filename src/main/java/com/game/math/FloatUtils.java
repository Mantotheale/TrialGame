package com.game.math;

import java.util.Comparator;

public class FloatUtils {
    private FloatUtils() { }

    public static boolean eq(float a, float b) {
        return Math.abs(a - b) <= MathConsts.FLOAT_ACCURACY_EPSILON;
    }

    public static boolean lt(float a, float b) {
        return a - b < -MathConsts.FLOAT_ACCURACY_EPSILON;
    }

    public static boolean leq(float a, float b) {
        return a - b <= MathConsts.FLOAT_ACCURACY_EPSILON;
    }

    public static boolean gt(float a, float b) {
        return a - b > MathConsts.FLOAT_ACCURACY_EPSILON;
    }

    public static boolean geq(float a, float b) {
        return a - b >= -MathConsts.FLOAT_ACCURACY_EPSILON;
    }

    public final static Comparator<Float> EPS_COMPARATOR = (o1, o2) -> {
        if (eq(o1, o2)) return 0;
        return lt(o1, o2) ? -1 : 1;
    };
}