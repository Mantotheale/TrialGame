package com.game.renderer.texture;

import com.game.math.Vec2f;

public interface Texture {
    int texId();
    int bitWidth();
    int bitHeight();
    float normalizedWidth();
    float normalizedHeight();
    Vec2f bottomLeft();
    Vec2f bottomRight();
    Vec2f topRight();
    Vec2f topLeft();
    void delete();
}
