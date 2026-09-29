package com.game;

import com.game.camera.Camera;
import com.game.camera.CameraProjection;
import com.game.event.*;
import com.game.event.deferred.CloseGameRequestedEvent;
import com.game.event.bus.EventBus;
import com.game.event.instant.*;
import com.game.fonts.FontData;
import com.game.fonts.FontUtils;
import com.game.fonts.GlyphData;
import com.game.input.*;
import com.game.math.*;
import com.game.renderer.Renderer;
import com.game.transform.*;
import com.game.util.Color;
import com.game.window.Window;
import com.game.window.WindowBuilder;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FreeType;
import org.lwjgl.util.msdfgen.MSDFGen;
import org.lwjgl.util.msdfgen.MSDFGenBitmap;
import org.lwjgl.util.msdfgen.MSDFGenTransform;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.lwjgl.BufferUtils.createByteBuffer;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImageWrite.stbi_flip_vertically_on_write;
import static org.lwjgl.stb.STBImageWrite.stbi_write_png;
import static org.lwjgl.system.MemoryStack.stackPush;
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

        //FontData fontData = FontUtils.openFont(Path.of("src/main/resources/fonts/NotoSansJP-Regular.ttf"));
        FontData fontData = FontUtils.openFont(Path.of("src/main/resources/fonts/JetBrainsMono-Regular.ttf"));
        float scale = 75f / fontData.fontSize();
        float lineHeight = fontData.lineHeight() * scale;

        String str = "Salve.\nBuongiorno!\nUn caffè, perfavore.\nEcco a lei.";

        List<Float> lineLengths = new ArrayList<>();
        int len = 0;
        for (int codePoint: str.codePoints().toArray()) {
            if (codePoint == '\n') {
                lineLengths.add(len * scale);
                len = 0;
                continue;
            }

            len += fontData.glyphData(fontData.getGlyphId(codePoint)).advanceWidth();
        }
        if (len != 0) lineLengths.add(len * scale);

        points = new ArrayList<>();
        lines = new ArrayList<>();

        int lineCounter = 0;
        Function<Integer, Vec2f> lineNumberLengthToPen = (lineNumber) ->
                new Vec2f(
                        -500 + ((1000 - lineLengths.get(lineNumber)) / 2),
                        500 - ((1000 - lineHeight * lineLengths.size()) / 2) - lineHeight * lineNumber
                );

        Vec2f pen = lineNumberLengthToPen.apply(0);
        for (int codePoint: str.codePoints().toArray()) {
            if (codePoint == '\n') {
                lineCounter++;
                pen = lineNumberLengthToPen.apply(lineCounter);
                continue;
            }

            int glyphId = fontData.getGlyphId(codePoint);
            GlyphData glyphData = fontData.glyphData(glyphId);

            Vec2f constPen = pen;
            List<FontPointFloat> glyphPoints = glyphData.points().stream()
                    .map(p -> new FontPointFloat(new Vec2f(p.x(), p.y()).scale(scale).add(constPen), p.onCurve()))
                    .toList();

            points.addAll(
                    glyphPoints.stream()
                            .map(fp -> new FontCircle(new Circle(fp.point, 5), fp.onCurve()))
                            .toList()
            );

            lines.addAll(glyphData.contours().stream()
                    .flatMap(c -> {
                        List<FontPointFloat> contourPoints = new ArrayList<>(
                                glyphPoints.subList(Short.toUnsignedInt(c.offset()), Short.toUnsignedInt(c.end()) + 1)
                        );

                        int firstOnCurveIdx;
                        for (firstOnCurveIdx = 0; firstOnCurveIdx < contourPoints.size(); firstOnCurveIdx++)
                            if (contourPoints.get(firstOnCurveIdx).onCurve) break;

                        Collections.rotate(contourPoints, -firstOnCurveIdx);
                        contourPoints.add(contourPoints.getFirst());

                        List<Segment> l = new ArrayList<>();
                        FontPointFloat previous = null;
                        FontPointFloat prePrevious = null;
                        for (FontPointFloat present : contourPoints) {
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
                                    FontPointFloat phantom = new FontPointFloat(middlePoint, true);
                                    BezierCurveOrder2 curve = new BezierCurveOrder2(prePrevious.point(), previous.point(), phantom.point());
                                    l.addAll(curve.linearize(15));
                                    prePrevious = phantom;
                                }
                            }

                            previous = present;
                        }

                        return l.stream();
                    }).toList()
            );

            pen = pen.add(new Vec2f(glyphData.advanceWidth() * scale, 0));
        }

        //System.out.println(points);
        //System.out.println(lines);
        ByteBuffer fontDataMSDF = null;
        try {
            fontDataMSDF = ioResourceToByteBuffer("fonts/JetBrainsMono-Regular.ttf", 512 * 1024);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try (MemoryStack stack = stackPush()) {
            PointerBuffer pp = stack.callocPointer(1);
            DoubleBuffer dp = stack.callocDouble(1);

            check(msdf_ft_set_load_callback(name -> FreeType.getLibrary().getFunctionAddress(memByteBuffer(name, memByteBufferNT1(name).capacity() + 1))));
            check(msdf_ft_init(pp));
            long ft = pp.get(0);

            check(msdf_ft_load_font_data(ft, fontDataMSDF, pp));
            long font = pp.get(0);

            if (dp.get(0) != 0.0) throw new AssertionError();
            check(msdf_ft_font_load_glyph(font, 'A', MSDF_FONT_SCALING_EM_NORMALIZED, dp, pp));
            if (dp.get(0) == 0.0) throw new AssertionError();

            long shape = pp.get(0);

            check(msdf_shape_normalize(shape));
            check(msdf_shape_edge_colors_simple(shape, 3.0));

            MSDFGenBitmap bitmap = MSDFGenBitmap.calloc(stack);
            check(msdf_bitmap_alloc(MSDF_BITMAP_TYPE_MSDF, 32, 32, bitmap));

            check(msdf_generate_msdf(bitmap, shape, MSDFGenTransform.calloc(stack)
                    .scale(it -> it
                            .x(32.0)
                            .y(32.0))
                    .translation(it -> it
                            .x(0.125)
                            .y(0.125))
                    .distance_mapping(it -> it.
                            lower(-0.5 * 0.125)
                            .upper(0.5 * 0.125))
            ));

            MSDFGenBitmap output = bitmap;
            /*
            MSDFGenBitmap output = MSDFGenBitmap.calloc(stack);
            check(msdf_bitmap_alloc(MSDF_BITMAP_TYPE_MSDF, 32, 32, output));

            msdf_render_sdf(output, msdfBitmapHandle);
            //*/

            IntBuffer pi = stack.mallocInt(1);
            msdf_bitmap_get_channel_count(output, pi);
            int channelCount = pi.get(0);

            ByteBuffer pixels = getBitmapU8(output);

            //msdf_bitmap_free(output);
            msdf_bitmap_free(bitmap);

            stbi_flip_vertically_on_write(true);
            stbi_write_png("msdfgen.png", output.width(), output.height(), channelCount, pixels, 0);

            memFree(pixels);

            nmsdf_ft_font_destroy(font);
            msdf_ft_deinit(ft);

            Objects.requireNonNull(msdf_ft_get_load_callback()).free();

            ByteBuffer mask = memAlloc(output.width() * output.height());
            for (int i = 0; i < mask.capacity(); i++) {
                int r = pixels.get(i * 3)     & 0xFF;
                int g = pixels.get(i * 3 + 1) & 0xFF;
                int b = pixels.get(i * 3 + 2) & 0xFF;
                int med = max(min(r, g), min(max(r, g), b));
                mask.put(i, (byte) (med > 127 ? 255 : 0));
            }
            stbi_write_png("msdfgen_mask.png", output.width(), output.height(), 1, mask, 0);
            memFree(mask);
        }

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

        ByteBuffer pixels = getBitmapU8(msdfBitmapHandle);
        msdf_bitmap_free(msdfBitmapHandle);

        stbi_flip_vertically_on_write(true);
        stbi_write_png("msdfgenOther.png", 32, 32, 3, pixels, 0);

        memFree(pixels);
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

    public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
        ByteBuffer buffer;

        Path path = resource.startsWith("http") ? null : Paths.get(resource);
        if (path != null && Files.isReadable(path)) {
            try (SeekableByteChannel fc = Files.newByteChannel(path)) {
                buffer = BufferUtils.createByteBuffer((int)fc.size() + 1);
                while (fc.read(buffer) != -1) {
                    ;
                }
            }
        } else {
            try (
                    InputStream source = resource.startsWith("http")
                            ? new URL(resource).openStream()
                            : Game.class.getClassLoader().getResourceAsStream(resource);
                    ReadableByteChannel rbc = Channels.newChannel(source)
            ) {
                buffer = createByteBuffer(bufferSize);

                while (true) {
                    int bytes = rbc.read(buffer);
                    if (bytes == -1) {
                        break;
                    }
                    if (buffer.remaining() == 0) {
                        buffer = resizeBuffer(buffer, buffer.capacity() * 3 / 2); // 50%
                    }
                }
            }
        }

        buffer.flip();
        return memSlice(buffer);
    }

    private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
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
        /*points.forEach(
                fp -> renderer.addCircle(
                        fp.circle,
                        fp.onCurve ? Color.GREEN : Color.RED
                )
        );*/

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

    record FontPointFloat(Vec2f point, boolean onCurve) { }

    record FontCircle(Circle circle, boolean onCurve) {
        public Vec2f point() {
            return circle.center();
        }
    }
}