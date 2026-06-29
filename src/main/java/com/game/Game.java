package com.game;

import com.game.camera.Camera;
import com.game.camera.CameraProjection;
import com.game.collision.CollisionManager;
import com.game.entity.Entity;
import com.game.entity.EntityManager;
import com.game.event.*;
import com.game.event.deferred.CloseGameRequestedEvent;
import com.game.event.bus.EventBus;
import com.game.event.deferred.EntityMovedEvent;
import com.game.event.deferred.PlaySoundRequestEvent;
import com.game.event.instant.*;
import com.game.input.*;
import com.game.math.Vec2f;
import com.game.renderer.Renderer;
import com.game.resourcemanager.ResourceManager;
import com.game.sound.Sound;
import com.game.sound.SoundDevice;
import com.game.sound.SoundManager;
import com.game.transform.*;
import com.game.window.Window;
import com.game.window.WindowBuilder;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

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
    private final EventBus eventBus;
    private final EntityManager entityManager;
    private final CollisionManager collisionManager;
    private final SoundManager soundManager;
    private final SoundDevice soundDevice;

    private final Entity reshiram;
    private final MewTwo mewtwo;
    private final TileMap map;

    private final Camera camera;
    private Vec2f mainCharacterLocation;

    private int updates;
    private int frames;

    private final ImGuiImplGlfw imguiGlfw;
    private final ImGuiImplGl3 imguiGl3;

    public Game() {
        System.out.println("My first game!");

        shouldClose = false;

        eventBus = new EventBus();
        eventBus.addObserver(RenderRequestEvent.class, this::onRenderRequested);
        eventBus.addObserver(CloseGameRequestedEvent.class, this::onCloseGameRequest);

        entityManager = new EntityManager(eventBus);
        collisionManager = new CollisionManager(eventBus);

        window = new WindowBuilder()
                .setTitle("Hello World!")
                .setWidth(720)
                .setHeight(720)
                .build();
        window.setVsync(false);

        renderer = new Renderer(eventBus);
        renderer.setClearColor(0.957f, 0.9062f, 0.5859f, 1.0f);

        inputManager = new InputManager(window, eventBus);

        soundDevice = new SoundDevice();
        resourceManager = new ResourceManager(Path.of("src/main/resources/atlases"), 0);

        soundManager = new SoundManager(eventBus, resourceManager);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        camera = new Camera(
                new CameraProjection.Orthographic(-8.5f, 8.5f, -8.5f, 8.5f, 0.01f, 20),
                new Transform3D(new Translation3D(0, 0, 20), Rotation3D.fromDirection(Rotation3D.WORLD_FRONT), new Scale3D()),
                eventBus
        );

        map = TileMap.fromFile(
                Path.of("src/main/resources/maps/simple_map.txt"),
                resourceManager,
                eventBus,
                entityManager,
                collisionManager
        );

        reshiram = new Reshiram(
                new Transform2D(new Translation2D(0, 0), Scale2D.UNIT, 2),
                resourceManager,
                eventBus,
                entityManager,
                collisionManager
        );
        camera.setEntityToFollow(() -> collisionManager.state(reshiram.id()).position());

        mewtwo = new MewTwo(
                new Transform2D(new Translation2D(4, 0), Scale2D.UNIT, 2),
                resourceManager,
                eventBus,
                entityManager,
                collisionManager
        );
        mewtwo.setTargetPosition(() -> collisionManager.state(reshiram.id()).position());

        eventBus.postEvent(PlaySoundRequestEvent.generateEvent(Sound.NATIONAL_PARK, true, 0.3f));
        updates = 0;
        frames = 0;

        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);

        imguiGlfw = new ImGuiImplGlfw();
        imguiGl3 = new ImGuiImplGl3();
        imguiGlfw.init(window.id(), true);
        imguiGl3.init("#version 330 core");

        mainCharacterLocation = Vec2f.ZERO;
        eventBus.addObserver(EntityMovedEvent.class, (_, event) -> {
            if (event.entityId().equals(reshiram.id()))
                mainCharacterLocation = event.position();
        });
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
        eventBus.dispatchDeferredEvents();
    }

    private void update() {
        updates++;

        eventBus.postEvent(new UpdateEvent(inputManager, resourceManager, entityManager, collisionManager));
        eventBus.dispatchDeferredEvents();

        collisionManager.simulate(eventBus, entityManager);
        eventBus.postEvent(new PhysicsUpdatedEvent(collisionManager));
        eventBus.dispatchDeferredEvents();

        eventBus.postEvent(new LateUpdateEvent());
        eventBus.dispatchDeferredEvents();
    }

    private void oneSecUpdate() {
        System.out.println("UPS: " + updates);
        System.out.println("FPS: " + frames);
        updates = 0;
        frames = 0;

        eventBus.postEvent(new OneSecUpdateEvent());
        eventBus.dispatchDeferredEvents();
    }

    private void render() {
        frames++;

        renderer.beginScene(camera);
        eventBus.postEvent(new RenderRequestEvent(renderer));
        renderer.addGrid(mainCharacterLocation);
        renderer.endScene();

        imguiGlfw.newFrame();
        imguiGl3.newFrame();
        ImGui.newFrame();

        ImGui.begin("Debug");
        ImGui.text("Daje");
        ImGui.end();

        ImGui.render();
        imguiGl3.renderDrawData(ImGui.getDrawData());

        window.swapBuffers();
    }

    private boolean shouldClose() {
        return shouldClose;
    }

    public void onRenderRequested(EventBus dispatcher, InstantEvent event) {
        if (event instanceof RenderRequestEvent(Renderer r))
            for (RenderComponent component: map)
                r.submit(component.transform(), component.texture());
    }

    public void onCloseGameRequest(EventBus dispatcher, DeferredEvent event) {
        if (event instanceof CloseGameRequestedEvent)
            shouldClose = true;
    }

    private void terminate() {
        imguiGl3.shutdown();
        imguiGlfw.shutdown();
        ImGui.destroyContext();
        reshiram.delete(eventBus);
        mewtwo.delete(eventBus);
        resourceManager.delete();
        renderer.delete(eventBus);
        window.delete();
        soundManager.delete();
        soundDevice.delete();
        eventBus.postEvent(new GameClosedEvent());
        eventBus.dispatchDeferredEvents();
    }
}