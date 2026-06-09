package com.game.renderer.texture;

import com.game.util.Vec2f;

public interface Texture {
    int id();
    int bitWidth();
    int bitHeight();
    float normalizedWidth();
    float normalizedHeight();
    Vec2f leftBottomCorner();
}
