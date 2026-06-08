package com.game;

import com.game.camera.Camera;
import com.game.camera.CameraProjection;
import com.game.input.*;
import com.game.renderer.Renderer;
import com.game.renderer.texture.Texture;
import com.game.renderer.texture.TextureAttributes;
import com.game.renderer.texture.TextureMagnifyingFilter;
import com.game.renderer.texture.TextureMinifyingFilter;
import com.game.transform.Rotation;
import com.game.transform.Scale;
import com.game.transform.Transform;
import com.game.transform.Translation;
import com.game.util.Observer;
import com.game.window.Window;
import com.game.window.WindowBuilder;

import java.nio.file.Path;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Game implements Observer<Input> {
    private final static double ONE_SEC_TIME = 1;
    private final static int UPDATES_PER_SECOND = 60;
    private final static double UPDATE_TIME = 1d / UPDATES_PER_SECOND;

    private final Window window;

    private final Renderer renderer;
    private final Texture texture1;
    private final Texture texture2;
    private Transform transform1;
    private Transform transform2;
    private final Camera camera;

    private boolean escPressed;

    private int updates;
    private int frames;

    public Game() {
        System.out.println("My first game!");

        window = new WindowBuilder().setTitle("Hello World!").build();
        window.addObserver(this);

        renderer = new Renderer();
        renderer.setClearColor(0.957f, 0.9062f, 0.5859f, 1.0f);

        TextureAttributes texAttr = new TextureAttributes.Builder()
                .magnifyingFilter(TextureMagnifyingFilter.LINEAR)
                .minifyingFilter(TextureMinifyingFilter.LINEAR_MIPMAP_LINEAR)
                .mipmap(true)
                .build();

        texture1 = new Texture(texAttr, Path.of("src/main/resources/images/reshiram.png"));
        texture2 = new Texture(texAttr, Path.of("src/main/resources/images/background.png"));

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        camera = new Camera(
                new CameraProjection.Orthographic(-5, 5, -5, 5, 0.01f, 20),
                new Transform(new Translation(0, 0, 20), Rotation.fromDirection(Rotation.WORLD_FRONT), new Scale())
        );

        transform1 = new Transform(new Translation(0, 0, 1), new Rotation(), new Scale());
        transform2 = new Transform(new Translation(), new Rotation(), new Scale(10));

        updates = 0;
        frames = 0;
    }

    public void run() {
        double currentTime = glfwGetTime();
        double nextUpdateTime = currentTime + UPDATE_TIME;
        double nextOneSecTime = currentTime + ONE_SEC_TIME;

        while (!shouldClose()) {
            processInputs();

            currentTime = glfwGetTime();
            while (currentTime >= nextUpdateTime) {
                update();
                nextUpdateTime += UPDATE_TIME;
            }

            render();

            while (currentTime >= nextOneSecTime) {
                oneSecUpdate();
                nextOneSecTime += ONE_SEC_TIME;
            }
        }

        terminate();
    }

    private void processInputs() {
        glfwPollEvents();
    }

    private void update() {
        updates++;

        if (glfwGetKey(window.id(), GLFW_KEY_W) == GLFW_PRESS) {
            transform1 = transform1.translate(0, 1 * (float) UPDATE_TIME, 0);
        }
        if (glfwGetKey(window.id(), GLFW_KEY_S) == GLFW_PRESS) {
            transform1 = transform1.translate(0, -1 * (float) UPDATE_TIME, 0);
        }
        if (glfwGetKey(window.id(), GLFW_KEY_A) == GLFW_PRESS) {
            transform1 = transform1.translate(-1 * (float) UPDATE_TIME, 0, 0);
        }
        if (glfwGetKey(window.id(), GLFW_KEY_D) == GLFW_PRESS) {
            transform1 = transform1.translate(1 * (float) UPDATE_TIME, 0, 0);
        }

        if (glfwGetKey(window.id(), GLFW_KEY_UP) == GLFW_PRESS) {
            camera.move(new Translation(0, 2 * (float) UPDATE_TIME, 0));
        }
        if (glfwGetKey(window.id(), GLFW_KEY_DOWN) == GLFW_PRESS) {
            camera.move(new Translation(0, -2 * (float) UPDATE_TIME, 0));
        }
        if (glfwGetKey(window.id(), GLFW_KEY_LEFT) == GLFW_PRESS) {
            camera.move(new Translation(-2 * (float) UPDATE_TIME, 0, 0));
        }
        if (glfwGetKey(window.id(), GLFW_KEY_RIGHT) == GLFW_PRESS) {
            camera.move(new Translation(2 * (float) UPDATE_TIME, 0, 0));
        }
    }

    private void oneSecUpdate() {
        System.out.println("UPS: " + updates);
        System.out.println("FPS: " + frames);
        updates = 0;
        frames = 0;
    }

    private void render() {
        frames++;

        renderer.beginScene(camera);
        renderer.submit(transform2, texture2);
        renderer.submit(transform1, texture1);
        renderer.endScene();

        window.swapBuffers();
    }

    private boolean shouldClose() {
        return escPressed || window.shouldClose();
    }

    @Override
    public void handle(Input value) {
        switch (value) {
            case KeyInput(PhysicalKey key, PhysicalAction action) -> {
                System.out.println("Key pressed: " + key + ", action: " + action);
                if (key == PhysicalKey.ESCAPE && action == PhysicalAction.RELEASE)
                    escPressed = true;
            }
            case ResizeFrameBuffer(int width, int height) -> {
                System.out.println("Frame buffer resized: (" + width + ", " + height + ")");
                renderer.setViewport(0, 0, width, height);
            }
            case CloseWindow() -> System.out.println("Close window clicked");
        }
    }

    private void terminate() {
        texture1.delete();
        texture2.delete();
        renderer.delete();
        window.delete();
    }
}