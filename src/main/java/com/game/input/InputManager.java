package com.game.input;

import com.game.event.CloseGameRequestedEvent;
import com.game.event.EventDispatcher;
import com.game.input.rawcomponents.KeyState;
import com.game.input.rawcomponents.PhysicalAction;
import com.game.input.rawcomponents.PhysicalKey;
import com.game.window.Window;

import java.util.Arrays;

public class InputManager implements InputObserver {
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
    public void onInput(Input input) {
        if (input instanceof KeyInput(PhysicalKey key, PhysicalAction action)) {
            switch (action) {
                case PRESS -> keyStates[key.ordinal()] = KeyState.DOWN;
                case RELEASE -> keyStates[key.ordinal()] = KeyState.UP;
            }
        }

        switch (input) {
            case CloseWindow() -> eventDispatcher.pushEvent(new CloseGameRequestedEvent());
            case KeyInput(PhysicalKey key, _) when key == PhysicalKey.ESCAPE -> eventDispatcher.pushEvent(new CloseGameRequestedEvent());
            default -> {}
        }
    }
}
