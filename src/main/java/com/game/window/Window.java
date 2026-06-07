package com.game.window;

import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;

public class Window {
    private final long id;

    Window(long id) {
        this.id = id;
    }

    public long id() {
        return id;
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(id);
    }

    public void setSwapInterval(int interval) {
        glfwSwapInterval(interval);
    }

    public void swapBuffers() {
        glfwSwapBuffers(id);
    }

    public void delete() {
        glfwFreeCallbacks(id);
        glfwDestroyWindow(id);

        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }
}
