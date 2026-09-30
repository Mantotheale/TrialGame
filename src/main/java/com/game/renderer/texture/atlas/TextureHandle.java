package com.game.renderer.texture.atlas;

import com.game.math.Vec2f;

public interface TextureHandle {
    int texId();
    Vec2f bottomLeft();
    Vec2f bottomRight();
    Vec2f topRight();
    Vec2f topLeft();
}
