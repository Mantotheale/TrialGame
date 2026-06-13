package com.game.window;

import com.game.input.*;

import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;

public class Window implements InputObservable {
    private final long id;
    private final Set<InputObserver> observers;

    Window(long id) {
        this.id = id;
        observers = new HashSet<>();

        glfwSetKeyCallback(id, this::keyCallback);
        glfwSetWindowCloseCallback(id, this::closeCallback);
        glfwSetFramebufferSizeCallback(id, this::frameBufferSizeCallback);
    }

    public long id() {
        return id;
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(id);
    }

    public void setVsync(boolean vsync) {
        glfwSwapInterval(vsync ? 1 : 0);
    }

    public void swapBuffers() {
        glfwSwapBuffers(id);
    }

    public void delete() {
        observers.clear();
        glfwFreeCallbacks(id);
        glfwDestroyWindow(id);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    @Override
    public void addObserver(InputObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(InputObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Input value) {
        for (InputObserver observer : observers)
            observer.onInput(value);
    }

    private void keyCallback(long _window, int key, int _scancode, int action, int _mods) {
        notifyObservers(KeyInput.fromGlfw(key, action));
    }

    private void closeCallback(long _window) {
        notifyObservers(CloseWindow.INSTANCE);
    }

    private void frameBufferSizeCallback(long _window, int width, int height) {
        notifyObservers(new ResizeFrameBuffer(width, height));
    }
}
