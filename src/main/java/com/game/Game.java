package com.game;

import com.game.camera.Camera;
import com.game.camera.CameraProjection;
import com.game.collision.CollisionManager;
import com.game.event.*;
import com.game.input.*;
import com.game.renderer.Renderer;
import com.game.renderer.texture.*;
import com.game.resourcemanager.ResourceManager;
import com.game.transform.*;
import com.game.window.Window;
import com.game.window.WindowBuilder;

import java.nio.file.Path;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Game {
    private final static double ONE_SEC_TIME = 1;
    public final static int UPDATES_PER_SECOND = 60;
    public final static double UPDATE_TIME = ONE_SEC_TIME / UPDATES_PER_SECOND;

    private boolean shouldClose;

    private final Window window;
    private final Renderer renderer;
    private final InputManager inputManager;
    private final ResourceManager resourceManager;
    private final EventDispatcher eventDispatcher;
    private final CollisionManager collisionManager;

    private final Entity reshiram;
    private final Entity mewtwo;
    private final TileMap map;

    private final Camera camera;

    private int updates;
    private int frames;
    private int elapsedSeconds;

    public Game() {
        System.out.println("My first game!");

        shouldClose = false;

        eventDispatcher = new EventDispatcher();
        eventDispatcher.addObserver(this::onCloseGameRequest);
        eventDispatcher.addObserver(this::onRenderRequested);

        window = new WindowBuilder()
                .setTitle("Hello World!")
                .setWidth(720)
                .setHeight(720)
                .build();
        window.setVsync(false);

        renderer = new Renderer();
        renderer.setClearColor(0.957f, 0.9062f, 0.5859f, 1.0f);

        inputManager = new InputManager(window, eventDispatcher);

        resourceManager = new ResourceManager(Path.of("src/main/resources/atlases"));

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        camera = new Camera(
                new CameraProjection.Orthographic(-7, 7, -7, 7, 0.01f, 20),
                new Transform3D(new Translation3D(0, 0, 20), Rotation3D.fromDirection(Rotation3D.WORLD_FRONT), new Scale3D())
        );
        eventDispatcher.addObserver(camera);

        collisionManager = new CollisionManager();
        eventDispatcher.addObserver(collisionManager);

        map = TileMap.fromFile(Path.of("src/main/resources/maps/simple_map.txt"), resourceManager);

        reshiram = new Reshiram(
                new Transform2D(new Translation2D(0, 0), Scale2D.UNIT, 2),
                resourceManager.getTexture(Tile.RESHIRAM),
                inputManager,
                collisionManager
        );
        eventDispatcher.addObserver(reshiram);

        mewtwo = new MewTwo(
                new Transform2D(new Translation2D(4, 0), Scale2D.UNIT, 2),
                resourceManager.getTexture(Tile.MEWTWO),
                resourceManager,
                collisionManager
        );
        eventDispatcher.addObserver(mewtwo);

        updates = 0;
        frames = 0;
        elapsedSeconds = 0;
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
        eventDispatcher.dispatchEvents();
    }

    private void update() {
        updates++;

        eventDispatcher.pushEvent(new StartUpdateEvent());
        eventDispatcher.dispatchEvents();

        collisionManager.findCollisions(eventDispatcher);
        eventDispatcher.dispatchEvents();

        eventDispatcher.pushEvent(new EndUpdateEvent());
        eventDispatcher.dispatchEvents();
    }

    private void oneSecUpdate() {
        System.out.println("UPS: " + updates);
        System.out.println("FPS: " + frames);
        updates = 0;
        frames = 0;
        elapsedSeconds++;

        eventDispatcher.pushEvent(new StartOneSecUpdateEvent());
        eventDispatcher.dispatchEvents();

        eventDispatcher.pushEvent(new EndOneSecUpdateEvent());
        eventDispatcher.dispatchEvents();
    }

    private void render() {
        frames++;

        renderer.beginScene(camera);

        eventDispatcher.pushEvent(new RenderRequestEvent(renderer));
        eventDispatcher.dispatchEvents();

        renderer.endScene();

        window.swapBuffers();
    }

    private boolean shouldClose() {
        return shouldClose;
    }

    public void onRenderRequested(EventDispatcher dispatcher, Event event) {
        if (event instanceof RenderRequestEvent(Renderer r)) {
            for (RenderComponent component: map)
                r.submit(component.transform(), component.texture());
        }
    }

    public void onCloseGameRequest(EventDispatcher dispatcher, Event event) {
        if (event instanceof CloseGameRequestedEvent) {
            System.out.println("Event received: " + event);
            shouldClose = true;
        }
    }

    private void terminate() {
        resourceManager.delete();
        renderer.delete();
        window.delete();
        eventDispatcher.pushEvent(new GameClosedEvent());
        eventDispatcher.dispatchEvents();
    }
}