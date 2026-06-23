package com.game.math;

public record IntersectionData(float t, Vec2f normal) implements Comparable<IntersectionData> {
    public IntersectionData {
        if (FloatUtils.lt(t, 0) || FloatUtils.gt(t, 1))
            throw new IllegalArgumentException("The intersection time should be between 0 and 1. Was " + t);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        return (obj instanceof IntersectionData(float t2, Vec2f n2))
                && FloatUtils.eq(t, t2)
                && normal.equals(n2);
    }

    @Override
    public int compareTo(IntersectionData o) {
        return FloatUtils.EPS_COMPARATOR.compare(this.t, o.t);
    }
}
