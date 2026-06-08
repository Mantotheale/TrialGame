package com.game.window;

import org.lwjgl.glfw.*;

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

    public void setKeyCallback(GLFWKeyCallbackI cb) {
        GLFWKeyCallback previousCb = glfwSetKeyCallback(id, cb);
        if (previousCb != null)
            previousCb.free();
    }

    public void setCursorPosCallback(GLFWCursorPosCallbackI cb) {
        GLFWCursorPosCallback previousCb = glfwSetCursorPosCallback(id, cb);
        if (previousCb != null)
            previousCb.free();
    }

    public void setCloseCallback(GLFWWindowCloseCallbackI cb) {
        GLFWWindowCloseCallback previousCb = glfwSetWindowCloseCallback(id, cb);
        if (previousCb != null)
            previousCb.free();
    }

    public void setFrameBufferSizeCallback(GLFWFramebufferSizeCallbackI cb) {
        GLFWFramebufferSizeCallback previousCb = glfwSetFramebufferSizeCallback(id, cb);
        if (previousCb != null)
            previousCb.free();
    }

    public void delete() {
        glfwFreeCallbacks(id);
        glfwDestroyWindow(id);

        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }
}
