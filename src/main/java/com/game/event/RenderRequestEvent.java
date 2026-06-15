package com.game.event;

import com.game.renderer.Renderer;

public record RenderRequestEvent(Renderer renderer) implements Event { }
