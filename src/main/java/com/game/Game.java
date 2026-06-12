package com.game;

import com.game.camera.Camera;
import com.game.camera.CameraProjection;
import com.game.input.*;
import com.game.input.KeyInput;
import com.game.input.rawcomponents.KeyState;
import com.game.input.rawcomponents.PhysicalAction;
import com.game.input.rawcomponents.PhysicalKey;
import com.game.renderer.Renderer;
import com.game.renderer.texture.*;
import com.game.resourcemanager.ResourceManager;
import com.game.transform.*;
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
    private final InputManager inputManager;
    private final ResourceManager resourceManager;

    private Transform2D transform1;
    private final TileMap map;

    private final Camera camera;

    private int updates;
    private int frames;

    public Game() {
        System.out.println("My first game!");

        window = new WindowBuilder()
                .setTitle("Hello World!")
                .setWidth(720)
                .setHeight(720)
                .build();
        window.setVsync(false);
        window.addObserver(this);

        renderer = new Renderer(window);
        renderer.setClearColor(0.957f, 0.9062f, 0.5859f, 1.0f);

        inputManager = new InputManager(window);

        resourceManager = new ResourceManager(Path.of("src/main/resources/atlases"));

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        camera = new Camera(
                new CameraProjection.Orthographic(-7, 7, -7, 7, 0.01f, 20),
                new Transform3D(new Translation3D(0, 0, 20), Rotation3D.fromDirection(Rotation3D.WORLD_FRONT), new Scale3D())
        );

        transform1 = new Transform2D(new Translation2D(0, 0), Scale2D.UNIT, 2);
        map = TileMap.fromFile(Path.of("src/main/resources/maps/simple_map.txt"), resourceManager);

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

        if (inputManager.keyState(PhysicalKey.W) == KeyState.DOWN) {
            transform1 = transform1.translate(0, 1 * (float) UPDATE_TIME);
        }
        if (inputManager.keyState(PhysicalKey.S) == KeyState.DOWN) {
            transform1 = transform1.translate(0, -1 * (float) UPDATE_TIME);
        }
        if (inputManager.keyState(PhysicalKey.A) == KeyState.DOWN) {
            transform1 = transform1.translate(-1 * (float) UPDATE_TIME, 0);
        }
        if (inputManager.keyState(PhysicalKey.D) == KeyState.DOWN) {
            transform1 = transform1.translate(1 * (float) UPDATE_TIME, 0);
        }

        if (inputManager.keyState(PhysicalKey.UP) == KeyState.DOWN) {
            camera.move(new Translation3D(0, 2 * (float) UPDATE_TIME, 0));
        }
        if (inputManager.keyState(PhysicalKey.DOWN) == KeyState.DOWN) {
            camera.move(new Translation3D(0, -2 * (float) UPDATE_TIME, 0));
        }
        if (inputManager.keyState(PhysicalKey.LEFT) == KeyState.DOWN) {
            camera.move(new Translation3D(-2 * (float) UPDATE_TIME, 0, 0));
        }
        if (inputManager.keyState(PhysicalKey.RIGHT) == KeyState.DOWN) {
            camera.move(new Translation3D(2 * (float) UPDATE_TIME, 0, 0));
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

        renderer.submit(transform1, resourceManager.getTexture(Tile.RESHIRAM));
        for (RenderComponent component: map)
            renderer.submit(component.transform(), component.texture());

        renderer.endScene();

        window.swapBuffers();
    }

    private boolean shouldClose() {
        return inputManager.keyState(PhysicalKey.ESCAPE) == KeyState.DOWN || window.shouldClose();
    }

    @Override
    public void handle(Input value) {
        switch (value) {
            case KeyInput(PhysicalKey key, PhysicalAction action) -> System.out.println("Key pressed: " + key + ", action: " + action);
            case CloseWindow() -> System.out.println("Close window clicked");
            default -> { }
        }
    }

    private void terminate() {
        resourceManager.delete();
        renderer.delete();
        window.delete();
    }
}