package com.game.input;

import com.game.input.rawcomponents.PhysicalAction;
import com.game.input.rawcomponents.PhysicalKey;

public record KeyInput(PhysicalKey key, PhysicalAction action) implements Input {
    public static KeyInput fromGlfw(int glfwKeyCode, int glfwActionCode) {
        return new KeyInput(PhysicalKey.fromGlfwCode(glfwKeyCode), PhysicalAction.fromGlfwCode(glfwActionCode));
    }
}
