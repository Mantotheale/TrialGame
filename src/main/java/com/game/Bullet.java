package com.game;

import com.game.event.*;
import com.game.renderer.Renderer;
import com.game.renderer.texture.Texture;
import com.game.transform.Transform2D;

import static com.game.Game.UPDATE_TIME;

public class Bullet extends Entity {
    boolean isJustCreated;
    int elapsedSeconds;

    public Bullet(Transform2D transform, Texture texture) {
        super(transform, texture);
        isJustCreated = true;
        elapsedSeconds = 0;
    }

    @Override
    public void onEvent(EventDispatcher dispatcher, Event event) {
        switch (event) {
            case RenderRequestEvent(Renderer renderer) -> renderer.submit(transform, texture);
            case StartUpdateEvent() -> transform = transform.translate(-1 * (float) UPDATE_TIME, 0);
            case StartOneSecUpdateEvent() -> {
                if (isJustCreated) {
                    isJustCreated = false;
                    return;
                }

                System.out.println("1 sec passed!");
                elapsedSeconds++;

                if (elapsedSeconds == 5) {
                    dispatcher.removeObserver(this);
                }
            }
            default -> { }
        }
    }
}
