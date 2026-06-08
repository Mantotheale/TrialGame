package com.game.input;

import com.game.input.rawcomponents.KeyState;
import com.game.input.rawcomponents.PhysicalAction;
import com.game.input.rawcomponents.PhysicalKey;
import com.game.util.Observer;
import com.game.window.Window;

import java.util.Arrays;

public class InputState implements Observer<Input> {
    private final KeyState[] keyStates;

    public InputState(Window window) {
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
