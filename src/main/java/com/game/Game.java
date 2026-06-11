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
import com.game.renderer.texture.atlas.AtlasGenerator;
import com.game.renderer.texture.atlas.AtlasLoader;
import com.game.renderer.texture.atlas.PathAndMetadata;
import com.game.renderer.texture.atlas.TextureAtlas;
import com.game.transform.*;
import com.game.util.Observer;
import com.game.window.Window;
import com.game.window.WindowBuilder;

import java.nio.file.Path;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Game implements Observer<Input> {
    private final static double ONE_SEC_TIME = 1;
    private final static int UPDATES_PER_SECOND = 60;
    private final static double UPDATE_TIME = 1d / UPDATES_PER_SECOND;

    private final Window window;
    private final Renderer renderer;
    private final InputState inputState;
    private final Texture reshiramTexture;
    private final SimpleTexture lowGrassTexture;
    private final SimpleTexture lakeBottomTexture;
    private final SimpleTexture lakeBottomRightTexture;
    private final SimpleTexture lakeRightTexture;
    private final SimpleTexture lakeTopRightTexture;
    private final SimpleTexture lakeTopTexture;
    private final SimpleTexture lakeTopLeftTexture;
    private final SimpleTexture lakeLeftTexture;
    private final SimpleTexture lakeBottomLeftTexture;
    private final SimpleTexture waterTexture;

    private Transform2D transform1;
    private final Transform2D[] lowGrassTransforms;
    private final Transform2D lakeBottomTransform;
    private final Transform2D lakeBottomRightTransform;
    private final Transform2D lakeRightTransform;
    private final Transform2D lakeTopRightTransform;
    private final Transform2D lakeTopTransform;
    private final Transform2D lakeTopLeftTransform;
    private final Transform2D lakeLeftTransform;
    private final Transform2D lakeBottomLeftTransform;
    private final Transform2D waterTransform;

    private final Camera camera;

    private int updates;
    private int frames;

    public Game() {
        Path atlasPath = Path.of("src/main/resources/atlases");
        AtlasLoader atlasLoader = new AtlasLoader(atlasPath);
        AtlasGenerator atlasGenerator = new AtlasGenerator(atlasPath, 1024);

        List<Tile> requestedTiles = List.of(Tile.values());
        var atlases = atlasLoader.loadAtlases(requestedTiles);
        if (atlases.isEmpty()) {
            System.out.println("Unable to load atlases");
            atlasGenerator.generateAtlases(requestedTiles);
            System.out.println("Successfully generated atlases");
        } else {
            System.out.println("Successfully loaded atlases");
            System.out.println(atlases.get().stream().map(PathAndMetadata::tilesMetadata).toList());
        }


        System.out.println("My first game!");

        window = new WindowBuilder().setTitle("Hello World!").build();
        window.addObserver(this);

        window.setVsync(false);

        System.out.println("Max texture size " + glGetInteger(GL_MAX_TEXTURE_SIZE));

        renderer = new Renderer(window);
        renderer.setClearColor(0.957f, 0.9062f, 0.5859f, 1.0f);

        inputState = new InputState(window);

        TextureAttributes texAttr = new TextureAttributes.Builder()
                .magnifyingFilter(TextureMagnifyingFilter.NEAREST)
                .minifyingFilter(TextureMinifyingFilter.NEAREST_MIPMAP_NEAREST)
                .mipmap(true)
                .build();

        PathAndMetadata reshiramPathMetadata = atlasLoader.loadAtlases(requestedTiles).get().getFirst();
        TextureAtlas atlas = new TextureAtlas(texAttr, reshiramPathMetadata.path(), reshiramPathMetadata.tilesMetadata());
        reshiramTexture = atlas.getFromTile(Tile.RESHIRAM);
        lowGrassTexture = new SimpleTexture(texAttr, Path.of("src/main/resources/tiles/grass.png"));
        lakeBottomTexture = new SimpleTexture(texAttr, Path.of("src/main/resources/tiles/lake_bottom.png"));
        lakeBottomRightTexture = new SimpleTexture(texAttr, Path.of("src/main/resources/tiles/lake_bottom_right.png"));
        lakeRightTexture = new SimpleTexture(texAttr, Path.of("src/main/resources/tiles/lake_right.png"));
        lakeTopRightTexture = new SimpleTexture(texAttr, Path.of("src/main/resources/tiles/lake_top_right.png"));
        lakeTopTexture = new SimpleTexture(texAttr, Path.of("src/main/resources/tiles/lake_top.png"));
        lakeTopLeftTexture = new SimpleTexture(texAttr, Path.of("src/main/resources/tiles/lake_top_left.png"));
        lakeLeftTexture = new SimpleTexture(texAttr, Path.of("src/main/resources/tiles/lake_left.png"));
        lakeBottomLeftTexture = new SimpleTexture(texAttr, Path.of("src/main/resources/tiles/lake_bottom_left.png"));
        waterTexture = new SimpleTexture(texAttr, Path.of("src/main/resources/tiles/water.png"));

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        camera = new Camera(
                new CameraProjection.Orthographic(-5, 5, -5, 5, 0.01f, 20),
                new Transform3D(new Translation3D(0, 0, 20), Rotation3D.fromDirection(Rotation3D.WORLD_FRONT), new Scale3D())
        );

        transform1 = new Transform2D(new Translation2D(0, 0), Scale2D.UNIT, 2);
        lowGrassTransforms = new Transform2D[(11 * 11) - 1];
        int idx = 0;
        for (int i = -5; i <= 5; i++) {
            for (int j = -5; j <= 5; j++) {
                if (i != 0 || j != 0) {
                    lowGrassTransforms[idx] = new Transform2D(new Translation2D(i, j), Scale2D.UNIT, 0);
                    idx++;
                }
            }
        }
        lakeBottomTransform = new Transform2D(new Translation2D(0, -1), Scale2D.UNIT, 1);
        lakeBottomRightTransform = new Transform2D(new Translation2D(1, -1), Scale2D.UNIT, 1);
        lakeRightTransform = new Transform2D(new Translation2D(1, 0), Scale2D.UNIT, 1);
        lakeTopRightTransform = new Transform2D(new Translation2D(1, 1), Scale2D.UNIT, 1);
        lakeTopTransform = new Transform2D(new Translation2D(0, 1), Scale2D.UNIT, 1);
        lakeTopLeftTransform = new Transform2D(new Translation2D(-1, 1), Scale2D.UNIT, 1);
        lakeLeftTransform = new Transform2D(new Translation2D(-1, 0), Scale2D.UNIT, 1);
        lakeBottomLeftTransform = new Transform2D(new Translation2D(-1, -1), Scale2D.UNIT, 1);
        waterTransform = new Transform2D(Translation2D.ORIGIN, Scale2D.UNIT, 0);

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

        renderer.submit(transform1, reshiramTexture);
        renderer.submit(lakeBottomTransform, lakeBottomTexture);
        renderer.submit(lakeBottomRightTransform, lakeBottomRightTexture);
        renderer.submit(lakeRightTransform, lakeRightTexture);
        renderer.submit(lakeTopRightTransform, lakeTopRightTexture);
        renderer.submit(lakeTopTransform, lakeTopTexture);
        renderer.submit(lakeTopLeftTransform, lakeTopLeftTexture);
        renderer.submit(lakeLeftTransform, lakeLeftTexture);
        renderer.submit(lakeBottomLeftTransform, lakeBottomLeftTexture);
        renderer.submit(waterTransform, waterTexture);

        for (Transform2D transform : lowGrassTransforms) {
            renderer.submit(transform, lowGrassTexture);
        }

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
        reshiramTexture.delete();
        lowGrassTexture.delete();
        lakeBottomTexture.delete();
        lakeBottomRightTexture.delete();
        lakeRightTexture.delete();
        lakeTopRightTexture.delete();
        lakeTopTexture.delete();
        lakeTopLeftTexture.delete();
        lakeLeftTexture.delete();
        lakeBottomLeftTexture.delete();
        waterTexture.delete();
        renderer.delete();
        window.delete();
    }
}