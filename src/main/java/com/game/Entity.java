package com.game;

import com.game.renderer.texture.Texture;
import com.game.transform.Transform2D;

public class Entity {
    protected Transform2D transform;
    protected Texture texture;

    public Entity(Transform2D transform, Texture texture) {
        this.transform = transform;
        this.texture = texture;
    }

    public Transform2D transform() {
        return transform;
    }

    public Texture texture() {
        return texture;
    }
}
