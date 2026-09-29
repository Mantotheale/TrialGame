package com.game;

import com.game.camera.Camera;
import com.game.camera.CameraProjection;
import com.game.event.*;
import com.game.event.deferred.CloseGameRequestedEvent;
import com.game.event.bus.EventBus;
import com.game.event.instant.*;
import com.game.input.*;
import com.game.math.*;
import com.game.renderer.Renderer;
import com.game.transform.*;
import com.game.window.Window;
import com.game.window.WindowBuilder;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FreeType;
import org.lwjgl.util.msdfgen.MSDFGen;
import org.lwjgl.util.msdfgen.MSDFGenBitmap;
import org.lwjgl.util.msdfgen.MSDFGenTransform;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImageWrite.stbi_flip_vertically_on_write;
import static org.lwjgl.stb.STBImageWrite.stbi_write_png;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.util.msdfgen.MSDFGen.*;
import static org.lwjgl.util.msdfgen.MSDFGenExt.*;

public class Game {
    private final static double ONE_SEC_TIME = 1;
    public final static int UPDATES_PER_SECOND = 60;
    public final static double UPDATE_TIME = ONE_SEC_TIME / UPDATES_PER_SECOND;

    private boolean shouldClose;

    private final Window window;
    private final Renderer renderer;
    private final InputManager inputManager;
    private final EventBus eventBus;

    private final Camera camera;

    private int updates;
    private int frames;

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

        int op_result = msdf_ft_set_load_callback(
                name -> FreeType.getLibrary()
                        .getFunctionAddress(MemoryUtil.memASCII(name))
        );
        if (op_result != MSDFGen.MSDF_SUCCESS) throw new AssertionError();

        PointerBuffer pointerBuffer = MemoryUtil.memCallocPointer(1);
        op_result = msdf_ft_init(pointerBuffer);
        if (op_result != MSDFGen.MSDF_SUCCESS) throw new AssertionError();
        long msdfgenHandle = pointerBuffer.get(0);
        if (msdfgenHandle == MemoryUtil.NULL) throw new AssertionError();
        System.out.println("MsdfGen Handle: " + msdfgenHandle);

        op_result = msdf_ft_load_font(msdfgenHandle, "src/main/resources/fonts/JetBrainsMono-Regular.ttf", pointerBuffer);
        if (op_result != MSDFGen.MSDF_SUCCESS) throw new AssertionError();
        long msdfFontHandle = pointerBuffer.get(0);
        if (msdfFontHandle == MemoryUtil.NULL) throw new AssertionError();
        System.out.println("MsdfFont Handle: " + msdfFontHandle);

        DoubleBuffer doubleBuffer = MemoryUtil.memCallocDouble(1);
        op_result = msdf_ft_font_load_glyph(msdfFontHandle, 'A', MSDF_FONT_SCALING_EM_NORMALIZED, doubleBuffer, pointerBuffer);
        if (op_result != MSDFGen.MSDF_SUCCESS) throw new AssertionError();
        long msdfShapeHandle = pointerBuffer.get(0);
        if (msdfShapeHandle == MemoryUtil.NULL) throw new AssertionError();
        double msdfAdvance = doubleBuffer.get(0);
        if (msdfAdvance == 0) throw new AssertionError();
        System.out.println("MsdfShape Handle: " + msdfShapeHandle);
        System.out.println("MsdfAdvance: " + msdfAdvance);

        op_result = msdf_shape_normalize(msdfShapeHandle);
        if (op_result != MSDFGen.MSDF_SUCCESS) throw new AssertionError();

        op_result = msdf_shape_edge_colors_simple(msdfShapeHandle, 3.0);
        if (op_result != MSDFGen.MSDF_SUCCESS) throw new AssertionError();

        MSDFGenBitmap msdfBitmapHandle = MSDFGenBitmap.calloc();
        op_result = msdf_bitmap_alloc(MSDF_BITMAP_TYPE_MSDF, 32, 32, msdfBitmapHandle);
        if (op_result != MSDFGen.MSDF_SUCCESS) throw new AssertionError();

        MSDFGenTransform msdfTransformHandle = MSDFGenTransform.calloc()
                .scale(s -> s.set(32.0, 32.0))
                .translation(t -> t.set(0.125, 0.125))
                .distance_mapping(r -> r.set(-0.5 * 0.125, 0.5 * 0.125));
        op_result = msdf_generate_msdf(msdfBitmapHandle, msdfShapeHandle, msdfTransformHandle);
        if (op_result != MSDFGen.MSDF_SUCCESS) throw new AssertionError();
        msdf_shape_free(msdfShapeHandle);
        msdfTransformHandle.free();

        ByteBuffer pixels = getBitmapU8(msdfBitmapHandle);
        msdf_bitmap_free(msdfBitmapHandle);
        msdfBitmapHandle.free();

        stbi_flip_vertically_on_write(true);
        stbi_write_png("msdfgen.png", 32, 32, 3, pixels, 0);

        memFree(pixels);

        msdf_ft_font_destroy(msdfFontHandle);
        msdf_ft_deinit(msdfgenHandle);

        Objects.requireNonNull(msdf_ft_get_load_callback()).free();
        memFree(pointerBuffer);
        memFree(doubleBuffer);
    }

    private static ByteBuffer getBitmapU8(MSDFGenBitmap bitmap) {
        PointerBuffer pp = MemoryUtil.memAllocPointer(1);

        check(msdf_bitmap_get_byte_size(bitmap, pp));
        long byteSize = pp.get(0);

        check(msdf_bitmap_get_pixels(bitmap, pp));
        FloatBuffer pixels = memFloatBuffer(pp.get(0), (int)byteSize >> 2);

        ByteBuffer data = memAlloc(pixels.capacity());
        for (int i = 0; i < pixels.capacity(); i++) {
            // clamp to [0, 1] range
            float v = Math.clamp(pixels.get(i), 0.0f, 1.0f);
            // half-down rounding (this is what msdfgen does)
            data.put(i, (byte)(~(int)(255.5f - 255.0f * v)));
            // half-up rounding
            //data.put(i, (byte)(255.f * v + 0.5f));
        }

        MemoryUtil.memFree(pp);
        return data;
    }

    private static void check(int result) {
        if (result != MSDF_SUCCESS) {
            throw new IllegalStateException("Operation failed with error code: " + result);
        }
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

        renderer.endScene();

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
}