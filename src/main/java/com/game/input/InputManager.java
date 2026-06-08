package com.game.input;

import com.game.util.Observer;
import com.game.window.Window;

import java.util.Arrays;

public class InputManager implements Observer<Input> {
    private final KeyState[] keyStates;

    public InputManager(Window window) {
        keyStates = new KeyState[PhysicalKey.values().length];
        Arrays.fill(keyStates, KeyState.UP);

        window.addObserver(this);
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
    }
}
