package com.game;

import com.game.event.*;
import com.game.input.InputManager;
import com.game.input.rawcomponents.KeyState;
import com.game.input.rawcomponents.PhysicalKey;
import com.game.renderer.texture.Texture;
import com.game.transform.Transform2D;

public class Player extends Entity implements EventObserver {
    private final InputManager inputManager;

    public Player(Transform2D transform, Texture texture, InputManager inputManager) {
        this.inputManager = inputManager;
        super(transform, texture);
    }

    @Override
    public void onEvent(EventDispatcher dispatcher, Event event) {
        if (event instanceof StartUpdateEvent) {
            if (inputManager.keyState(PhysicalKey.W) == KeyState.DOWN)
                transform = transform.translate(0, 1 * (float) Game.UPDATE_TIME);
            if (inputManager.keyState(PhysicalKey.S) == KeyState.DOWN)
                transform = transform.translate(0, -1 * (float) Game.UPDATE_TIME);
            if (inputManager.keyState(PhysicalKey.A) == KeyState.DOWN)
                transform = transform.translate(-1 * (float) Game.UPDATE_TIME, 0);
            if (inputManager.keyState(PhysicalKey.D) == KeyState.DOWN)
                transform = transform.translate(1 * (float) Game.UPDATE_TIME, 0);

            dispatcher.pushEvent(new EntityMovedEvent(this));
        }
    }
}
