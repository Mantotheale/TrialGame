package com.game.renderer.texture;

import com.game.util.Vec2f;

public interface Texture {
    int texId();
    int bitWidth();
    int bitHeight();
    float normalizedWidth();
    float normalizedHeight();
    Vec2f bottomLeftCorner();
    void delete();
}
