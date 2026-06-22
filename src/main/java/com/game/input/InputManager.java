package com.game.input;

import com.game.event.deferred.CloseGameRequestedEvent;
import com.game.event.bus.EventBus;
import com.game.input.rawcomponents.KeyState;
import com.game.input.rawcomponents.PhysicalAction;
import com.game.input.rawcomponents.PhysicalKey;
import com.game.window.Window;

import java.util.Arrays;

public class InputManager implements InputObserver {
    private final KeyState[] keyStates;
    private final EventBus eventBus;

    public InputManager(Window window, EventBus eventBus) {
        keyStates = new KeyState[PhysicalKey.values().length];
        Arrays.fill(keyStates, KeyState.UP);
        window.addObserver(this);

        this.eventBus = eventBus;
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
            case CloseWindow() -> eventBus.postEvent(new CloseGameRequestedEvent());
            case KeyInput(PhysicalKey key, _) when key == PhysicalKey.ESCAPE ->
                    eventBus.postEvent(new CloseGameRequestedEvent());
            default -> {}
        }
    }
}
