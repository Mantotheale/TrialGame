package com.game;

import com.game.event.*;
import com.game.renderer.Renderer;
import com.game.renderer.texture.Texture;
import com.game.transform.Transform2D;

import static com.game.Game.UPDATES_PER_SECOND;
import static com.game.Game.UPDATE_TIME;

public class Bullet extends Entity {
    int elapsedUpdates;

    public Bullet(Transform2D transform, Texture texture) {
        super(transform, texture);
        elapsedUpdates = 0;
    }

    @Override
    public void onEvent(EventDispatcher dispatcher, Event event) {
        switch (event) {
            case RenderRequestEvent(Renderer renderer) -> renderer.submit(transform, texture);
            case StartUpdateEvent() -> {
                elapsedUpdates++;
                if (elapsedUpdates == 5 * UPDATES_PER_SECOND) {
                    dispatcher.removeObserver(this);
                } else {
                    transform = transform.translate(-1 * (float) UPDATE_TIME, 0);
                    dispatcher.pushEvent(new EntityMovedEvent(this, transform.translation().toVec2f()));
                }
            }
            default -> { }
        }
    }
}
