package com.game.event.instant;

import com.game.event.InstantEvent;
import com.game.renderer.Renderer;

public record RenderRequestEvent(Renderer renderer) implements InstantEvent { }
