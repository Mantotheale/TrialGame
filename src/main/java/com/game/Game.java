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
    private final InputState inputState;
    private final ResourceManager resourceManager;

    private Transform2D transform1;
    private final Transform2D[] lowGrassTransforms;
    private final Transform2D[] waterTransforms;
    private final Transform2D[] lakeBottomTransforms;
    private final Transform2D[] lakeRightTransforms;
    private final Transform2D[] lakeTopTransforms;
    private final Transform2D[] lakeLeftTransforms;
    private final Transform2D lakeBottomRightTransform;
    private final Transform2D lakeTopRightTransform;
    private final Transform2D lakeTopLeftTransform;
    private final Transform2D lakeBottomLeftTransform;

    private final Camera camera;

    private int updates;
    private int frames;

    public Game() {
        System.out.println("My first game!");

        window = new WindowBuilder().setTitle("Hello World!").build();
        window.setVsync(false);
        window.addObserver(this);

        renderer = new Renderer(window);
        renderer.setClearColor(0.957f, 0.9062f, 0.5859f, 1.0f);

        inputState = new InputState(window);

        resourceManager = new ResourceManager(Path.of("src/main/resources/atlases"));



        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        camera = new Camera(
                new CameraProjection.Orthographic(-5, 5, -5, 5, 0.01f, 20),
                new Transform3D(new Translation3D(0, 0, 20), Rotation3D.fromDirection(Rotation3D.WORLD_FRONT), new Scale3D())
        );

        transform1 = new Transform2D(new Translation2D(0, 0), Scale2D.UNIT, 2);
        lowGrassTransforms = new Transform2D[(11 * 11) - (3 * 3)];
        int idx = 0;
        for (int i = -5; i <= 5; i++) {
            for (int j = -5; j <= 5; j++) {
                if (i < -1 || i > 1 || j < -1 || j > 1) {
                    lowGrassTransforms[idx] = new Transform2D(new Translation2D(i, j), Scale2D.UNIT, 0);
                    idx++;
                }
            }
        }

        idx = 0;
        waterTransforms = new Transform2D[3 * 3];
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                waterTransforms[idx] = new Transform2D(new Translation2D(i, j), Scale2D.UNIT, 0);
                idx++;
            }
        }

        lakeBottomTransforms = new Transform2D[3];
        lakeTopTransforms = new Transform2D[3];
        lakeRightTransforms = new Transform2D[3];
        lakeLeftTransforms = new Transform2D[3];
        for (int i = 0; i < 3; i++) {
            lakeBottomTransforms[i] = new Transform2D(new Translation2D(-1 + i, -2), Scale2D.UNIT, 1);
            lakeTopTransforms[i] = new Transform2D(new Translation2D(-1 + i, 2), Scale2D.UNIT, 1);
            lakeRightTransforms[i] = new Transform2D(new Translation2D(2, -1 + i), Scale2D.UNIT, 1);
            lakeLeftTransforms[i] = new Transform2D(new Translation2D(-2, -1 + i), Scale2D.UNIT, 1);
        }
        lakeBottomRightTransform = new Transform2D(new Translation2D(2, -2), Scale2D.UNIT, 1);
        lakeTopRightTransform = new Transform2D(new Translation2D(2, 2), Scale2D.UNIT, 1);
        lakeTopLeftTransform = new Transform2D(new Translation2D(-2, 2), Scale2D.UNIT, 1);
        lakeBottomLeftTransform = new Transform2D(new Translation2D(-2, -2), Scale2D.UNIT, 1);

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

        if (inputState.keyState(PhysicalKey.W) == KeyState.DOWN) {
            transform1 = transform1.translate(0, 1 * (float) UPDATE_TIME);
        }
        if (inputState.keyState(PhysicalKey.S) == KeyState.DOWN) {
            transform1 = transform1.translate(0, -1 * (float) UPDATE_TIME);
        }
        if (inputState.keyState(PhysicalKey.A) == KeyState.DOWN) {
            transform1 = transform1.translate(-1 * (float) UPDATE_TIME, 0);
        }
        if (inputState.keyState(PhysicalKey.D) == KeyState.DOWN) {
            transform1 = transform1.translate(1 * (float) UPDATE_TIME, 0);
        }

        if (inputState.keyState(PhysicalKey.UP) == KeyState.DOWN) {
            camera.move(new Translation3D(0, 2 * (float) UPDATE_TIME, 0));
        }
        if (inputState.keyState(PhysicalKey.DOWN) == KeyState.DOWN) {
            camera.move(new Translation3D(0, -2 * (float) UPDATE_TIME, 0));
        }
        if (inputState.keyState(PhysicalKey.LEFT) == KeyState.DOWN) {
            camera.move(new Translation3D(-2 * (float) UPDATE_TIME, 0, 0));
        }
        if (inputState.keyState(PhysicalKey.RIGHT) == KeyState.DOWN) {
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
        renderer.submit(lakeBottomRightTransform, resourceManager.getTexture(Tile.LAKE_BOTTOM_RIGHT));
        renderer.submit(lakeTopRightTransform, resourceManager.getTexture(Tile.LAKE_TOP_RIGHT));
        renderer.submit(lakeTopLeftTransform, resourceManager.getTexture(Tile.LAKE_TOP_LEFT));
        renderer.submit(lakeBottomLeftTransform, resourceManager.getTexture(Tile.LAKE_BOTTOM_LEFT));
        for (Transform2D transform : lakeTopTransforms)
            renderer.submit(transform, resourceManager.getTexture(Tile.LAKE_TOP));
        for (Transform2D transform : lakeBottomTransforms)
            renderer.submit(transform, resourceManager.getTexture(Tile.LAKE_BOTTOM));
        for (Transform2D transform : lakeRightTransforms)
            renderer.submit(transform, resourceManager.getTexture(Tile.LAKE_RIGHT));
        for (Transform2D transform : lakeLeftTransforms)
            renderer.submit(transform, resourceManager.getTexture(Tile.LAKE_LEFT));
        for (Transform2D transform : waterTransforms)
            renderer.submit(transform, resourceManager.getTexture(Tile.WATER));
        for (Transform2D transform : lowGrassTransforms)
            renderer.submit(transform, resourceManager.getTexture(Tile.GRASS));

        renderer.endScene();

        window.swapBuffers();
    }

    private boolean shouldClose() {
        return inputState.keyState(PhysicalKey.ESCAPE) == KeyState.DOWN || window.shouldClose();
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