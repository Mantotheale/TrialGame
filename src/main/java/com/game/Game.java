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
import com.game.fonts.FontData;
import com.game.fonts.FontUtils;
import com.game.fonts.GlyphData;
import com.game.input.*;
import com.game.math.*;
import com.game.renderer.Renderer;
import com.game.resourcemanager.ResourceManager;
import com.game.sound.Sound;
import com.game.sound.SoundDevice;
import com.game.sound.SoundManager;
import com.game.transform.*;
import com.game.util.Color;
import com.game.window.Window;
import com.game.window.WindowBuilder;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
    //private final ResourceManager resourceManager;
    private final EventBus eventBus;
    //private final EntityManager entityManager;
    //private final CollisionManager collisionManager;
    //private final SoundManager soundManager;
    //private final SoundDevice soundDevice;

    //private final Entity reshiram;
    //private final MewTwo mewtwo;
    //private final TileMap map;

    private final Camera camera;
    private Vec2f mainCharacterLocation;

    private int updates;
    private int frames;

    List<FontCircle> points;
    List<Segment> lines;

    //private final ImGuiImplGlfw imguiGlfw;
    //private final ImGuiImplGl3 imguiGl3;

    public Game() {
        System.out.println("My first game!");

        shouldClose = false;

        eventBus = new EventBus();
        eventBus.addObserver(RenderRequestEvent.class, this::onRenderRequested);
        eventBus.addObserver(CloseGameRequestedEvent.class, this::onCloseGameRequest);

        //entityManager = new EntityManager(eventBus);
        //collisionManager = new CollisionManager(eventBus);

        window = new WindowBuilder()
                .setTitle("Hello World!")
                .setWidth(1000)
                .setHeight(1000)
                .build();
        window.setVsync(false);

        renderer = new Renderer(eventBus, new Vec2f(720, 720));
        renderer.setClearColor(0.075f, 0.075f, 0.1f, 1.0f);

        inputManager = new InputManager(window, eventBus);

        //soundDevice = new SoundDevice();
        //resourceManager = new ResourceManager(Path.of("src/main/resources/atlases"), 0);

        //soundManager = new SoundManager(eventBus, resourceManager);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        camera = new Camera(
                new CameraProjection.Orthographic(-500, 500, -500, 500, 0.01f, 20),
                new Transform3D(new Translation3D(0, 0, 20), Rotation3D.fromDirection(Rotation3D.WORLD_FRONT), new Scale3D()),
                eventBus
        );

        /*map = TileMap.fromFile(
                Path.of("src/main/resources/maps/simple_map.txt"),
                resourceManager,
                eventBus,
                entityManager,
                collisionManager
        );*/

        /*reshiram = new Reshiram(
                new Transform2D(new Translation2D(0, 0), Scale2D.UNIT, 2),
                resourceManager,
                eventBus,
                entityManager,
                collisionManager
        );
        camera.setEntityToFollow(() -> collisionManager.state(reshiram.id()).position());*/

        /*mewtwo = new MewTwo(
                new Transform2D(new Translation2D(4, 0), Scale2D.UNIT, 2),
                resourceManager,
                eventBus,
                entityManager,
                collisionManager
        );
        mewtwo.setTargetPosition(() -> collisionManager.state(reshiram.id()).position());*/

        //eventBus.postEvent(PlaySoundRequestEvent.generateEvent(Sound.NATIONAL_PARK, true, 0.3f));
        updates = 0;
        frames = 0;

        /*ImGui.createContext();
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
        });*/

        //FontData fontData = FontUtils.openFont(Path.of("src/main/resources/fonts/JetBrainsMono-Regular.ttf"));
        //int glyphId = fontData.getGlyphId('P');
        FontData fontData = FontUtils.openFont(Path.of("src/main/resources/fonts/NotoSansJP-Regular.ttf"));
        int glyphId = fontData.getGlyphId('珠');
        GlyphData glyphData = fontData.glyphData(glyphId);

        Rectangle bbox = glyphData.boundingBox();
        Vec2f displayBoxSize = new Vec2f(900, 900);
        Vec2f glyphScaling = displayBoxSize.div(new Vec2f(bbox.width(), bbox.height()));
        Vec2f glyphTranspose = bbox.center().scale(glyphScaling).negate();

        points = glyphData.points().stream()
                .map(fp ->
                        new FontCircle(
                                new Circle(
                                        new Vec2f(fp.x(), fp.y()).scale(glyphScaling).add(glyphTranspose),
                                        5
                                ),
                                fp.onCurve()
                        )
                ).toList();

        lines = glyphData.contours().stream()
                .flatMap(c -> {
                    List<FontCircle> fontCircles = new ArrayList<>(points.subList(Short.toUnsignedInt(c.offset()), Short.toUnsignedInt(c.end()) + 1));

                    int firstOnCurveIdx;
                    for (firstOnCurveIdx = 0; firstOnCurveIdx < fontCircles.size(); firstOnCurveIdx++)
                        if (fontCircles.get(firstOnCurveIdx).onCurve) break;

                    Collections.rotate(fontCircles, -firstOnCurveIdx);
                    fontCircles.add(fontCircles.getFirst());

                    List<Segment> l = new ArrayList<>();
                    FontCircle previous = null;
                    FontCircle prePrevious = null;
                    for (FontCircle present : fontCircles) {
                        if (previous == null) {
                            previous = present;
                            continue;
                        }

                        if (present.onCurve) {
                            if (previous.onCurve) {
                                l.add(new Segment(previous.point(), present.point()));
                            } else {
                                BezierCurveOrder2 curve = new BezierCurveOrder2(prePrevious.point(), previous.point(), present.point());
                                l.addAll(curve.linearize(15));
                            }
                        } else {
                            if (previous.onCurve) {
                                prePrevious = previous;
                            } else {
                                Vec2f middlePoint = present.point().middlePoint(previous.point());
                                FontCircle phantom = new FontCircle(new Circle(middlePoint, present.circle.radius()), true);
                                BezierCurveOrder2 curve = new BezierCurveOrder2(prePrevious.point(), previous.point(), phantom.point());
                                l.addAll(curve.linearize(15));
                                prePrevious = phantom;
                            }
                        }

                        previous = present;
                    }

                    return l.stream();
                }).toList();
        System.out.println(points);
        System.out.println(lines);
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

        //eventBus.postEvent(new UpdateEvent(inputManager, resourceManager, entityManager, collisionManager));
        eventBus.dispatchDeferredEvents();

        //collisionManager.simulate(eventBus, entityManager);
        //eventBus.postEvent(new PhysicsUpdatedEvent(collisionManager));
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
        //renderer.addGrid(mainCharacterLocation);
        //renderer.addRect(new Rectangle(new Vec2f(0, 10), 5, 7), 0.1f, 0.5f, 0.5f, 1);
        //renderer.addSegment(new Segment(new Vec2f(13, 13), new Vec2f(13, -13)), 1, 0.7f, 0.2f, 0.4f, 1);
        //renderer.addSegment(new Segment(new Vec2f(13, 13), new Vec2f(20, -13)), 3, 0.3f, 0.6f, 0.7f, 1);
        //renderer.addCircle(new Circle(new Vec2f(-15, 15), 4), 0.7f, 0.2f, 0.1f, 1);
        points.forEach(
                fp -> renderer.addCircle(
                        fp.circle,
                        fp.onCurve ? Color.GREEN : Color.RED
                )
        );

        lines.forEach(l -> renderer.addSegment(l, 1, Color.BLUE));

        renderer.endScene();

        /*imguiGlfw.newFrame();
        imguiGl3.newFrame();
        ImGui.newFrame();

        ImGui.begin("Debug");
        ImGui.text("Daje");
        ImGui.end();

        ImGui.render();
        imguiGl3.renderDrawData(ImGui.getDrawData());*/

        window.swapBuffers();
    }

    private boolean shouldClose() {
        return shouldClose;
    }

    public void onRenderRequested(EventBus dispatcher, InstantEvent event) {
        /*if (event instanceof RenderRequestEvent(Renderer r))
            for (RenderComponent component: map)
                r.submit(component.transform(), component.texture());*/
    }

    public void onCloseGameRequest(EventBus dispatcher, DeferredEvent event) {
        if (event instanceof CloseGameRequestedEvent)
            shouldClose = true;
    }

    private void terminate() {
        /*imguiGl3.shutdown();
        imguiGlfw.shutdown();
        ImGui.destroyContext();
        reshiram.delete(eventBus);
        mewtwo.delete(eventBus);
        resourceManager.delete();*/
        renderer.delete(eventBus);
        window.delete();
        //soundManager.delete();
        //soundDevice.delete();
        eventBus.postEvent(new GameClosedEvent());
        eventBus.dispatchDeferredEvents();
    }

    record FontCircle(Circle circle, boolean onCurve) {
        public Vec2f point() {
            return circle.center();
        }
    }
}