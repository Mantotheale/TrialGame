package com.game;

import com.game.event.Event;
import com.game.event.EventDispatcher;
import com.game.renderer.texture.Texture;
import com.game.transform.Transform2D;

public class MapEntity extends Entity {
    public MapEntity(Transform2D transform, Texture texture) {
        super(transform, texture);
    }

    @Override
    public void onEvent(EventDispatcher dispatcher, Event event) {

    }
}
