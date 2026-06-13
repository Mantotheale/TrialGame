package com.game.input;

import com.game.event.CloseGameRequestEvent;
import com.game.event.EventDispatcher;
import com.game.input.rawcomponents.KeyState;
import com.game.input.rawcomponents.PhysicalAction;
import com.game.input.rawcomponents.PhysicalKey;
import com.game.util.Observer;
import com.game.window.Window;

import java.util.Arrays;

public class InputManager implements Observer<Input> {
    private final KeyState[] keyStates;
    private final EventDispatcher eventDispatcher;

    public InputManager(Window window, EventDispatcher eventDispatcher) {
        keyStates = new KeyState[PhysicalKey.values().length];
        Arrays.fill(keyStates, KeyState.UP);
        window.addObserver(this);

        this.eventDispatcher = eventDispatcher;
    }

    public KeyState keyState(PhysicalKey key) {
        return keyStates[key.ordinal()];
    }

    @Override
    public void handle(Input value) {
        if (value instanceof KeyInput(PhysicalKey key, PhysicalAction action)) {
            switch (action) {
                case PRESS -> keyStates[key.ordinal()] = KeyState.DOWN;
                case RELEASE -> keyStates[key.ordinal()] = KeyState.UP;
            }
        }

        switch (value) {
            case CloseWindow() -> eventDispatcher.notifyObservers(new CloseGameRequestEvent());
            case KeyInput(PhysicalKey key, _) when key == PhysicalKey.ESCAPE -> eventDispatcher.notifyObservers(new CloseGameRequestEvent());
            default -> {}
        }
    }
}
