package com.game;

import com.game.event.InstantEvent;
import com.game.event.bus.EventBus;
import com.game.renderer.texture.Texture;
import com.game.transform.Transform2D;

public class MapEntity extends Entity {
    public MapEntity(Transform2D transform, Texture texture) {
        super(transform, texture);
    }


    @Override
    public void onEvent(EventBus bus, InstantEvent event) {

    }
}
