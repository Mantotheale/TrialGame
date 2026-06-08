package com.game.input;

import static org.lwjgl.glfw.GLFW.*;

public enum PhysicalAction {
    PRESS(GLFW_PRESS),
    RELEASE(GLFW_RELEASE),
    REPEAT(GLFW_REPEAT);

    PhysicalAction(int glfwCode) { }

    public static PhysicalAction fromGlfwCode(int glfwCode) {
        if (glfwCode < GLFW_RELEASE || glfwCode > GLFW_REPEAT) throw new IllegalArgumentException("Invalid glfw action code: " + glfwCode);

        return GLFW_MAPPING[glfwCode];
    }

    private final static PhysicalAction[] GLFW_MAPPING = new PhysicalAction[] { RELEASE, PRESS, REPEAT };
}
