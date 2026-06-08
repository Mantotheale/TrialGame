package com.game.window;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.system.MemoryUtil.NULL;

public class WindowBuilder {
    private final static int DEFAULT_WIDTH = 720;
    private final static int DEFAULT_HEIGHT = 720;

    private String title = "Window";
    private int width = DEFAULT_WIDTH;
    private int height = DEFAULT_HEIGHT;
    private OpenGLVersion openGLVersion = OpenGLVersion.CORE_3_3;
    private boolean isResizable = true;
    private boolean isDecorated = true;
    private boolean isFocused = true;
    private boolean isCentered = true;

    public WindowBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public WindowBuilder setWidth(int width) {
        this.width = width;
        return this;
    }

    public WindowBuilder setHeight(int height) {
        this.height = height;
        return this;
    }

    public WindowBuilder setOpenGLVersion(OpenGLVersion openGLVersion) {
        this.openGLVersion = openGLVersion;
        return this;
    }

    public WindowBuilder setResizable(boolean resizable) {
        this.isResizable = resizable;
        return this;
    }

    public WindowBuilder setDecorated(boolean decorated) {
        this.isDecorated = decorated;
        return this;
    }

    public WindowBuilder setFocused(boolean focused) {
        this.isFocused = focused;
        return this;
    }

    public WindowBuilder setCentered(boolean centered) {
        this.isCentered = centered;
        return this;
    }

    public Window build() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, openGLVersion.major());
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, openGLVersion.minor());
        glfwWindowHint(GLFW_OPENGL_PROFILE, openGLVersion.profile());
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, openGLVersion.isForwardCompatible() ? GLFW_TRUE : GLFW_FALSE);

        glfwWindowHint(GLFW_RESIZABLE, isResizable ? GLFW_TRUE : GLFW_FALSE);
        glfwWindowHint(GLFW_DECORATED, isDecorated ? GLFW_TRUE : GLFW_FALSE);
        glfwWindowHint(GLFW_FOCUSED, isFocused ? GLFW_TRUE : GLFW_FALSE);

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);

        long window = glfwCreateWindow(width, height, title, NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        if (isCentered) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer pWidth = stack.mallocInt(1);
                IntBuffer pHeight = stack.mallocInt(1);

                glfwGetWindowSize(window, pWidth, pHeight);

                GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
                glfwSetWindowPos(
                        window,
                        (vidmode.width() - pWidth.get(0)) / 2,
                        (vidmode.height() - pHeight.get(0)) / 2
                );
            }
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        GL.createCapabilities();

        return new Window(window);
    }
}