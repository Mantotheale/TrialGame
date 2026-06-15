package com.game.transform;

import com.game.util.Vec2f;

public record Transform2D(Translation2D translation, Scale2D scale, int zIndex) {
    public Vec2f transform(Vec2f vec) {
        return translation.transform(scale.transform(vec));
    }

    public Transform2D translate(Translation2D translation) {
        return new Transform2D(this.translation.compose(translation), scale, zIndex);
    }

    public Transform2D translate(float x, float y) {
        return new Transform2D(this.translation.compose(x, y), scale, zIndex);
    }

    public Transform2D scale(float s) {
        return new Transform2D(translation, scale.compose(s), zIndex);
    }
}