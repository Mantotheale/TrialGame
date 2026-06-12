package com.game;

import com.game.renderer.texture.Texture;
import com.game.transform.Transform2D;

public record RenderComponent(Transform2D transform, Texture texture) { }
