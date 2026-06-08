package com.game.input;

public record KeyInput(PhysicalKey key, PhysicalAction action) implements Input {
    public static KeyInput fromGlfw(int glfwKeyCode, int glfwActionCode) {
        return new KeyInput(PhysicalKey.fromGlfwCode(glfwKeyCode), PhysicalAction.fromGlfwCode(glfwActionCode));
    }
}
