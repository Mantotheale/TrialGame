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
    private final Texture lowGrassTexture;
    private final Texture lakeBottomTexture;
    private final Texture lakeBottomRightTexture;
    private final Texture lakeRightTexture;
    private final Texture lakeTopRightTexture;
    private final Texture lakeTopTexture;
    private final Texture lakeTopLeftTexture;
    private final Texture lakeLeftTexture;
    private final Texture lakeBottomLeftTexture;
    private final Texture waterTexture;

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
        lowGrassTexture = atlas.getFromTile(Tile.GRASS);
        lakeBottomTexture = atlas.getFromTile(Tile.LAKE_BOTTOM);
        lakeBottomRightTexture = atlas.getFromTile(Tile.LAKE_BOTTOM_RIGHT);
        lakeRightTexture = atlas.getFromTile(Tile.LAKE_RIGHT);
        lakeTopRightTexture = atlas.getFromTile(Tile.LAKE_TOP_RIGHT);
        lakeTopTexture = atlas.getFromTile(Tile.LAKE_TOP);
        lakeTopLeftTexture = atlas.getFromTile(Tile.LAKE_TOP_LEFT);
        lakeLeftTexture = atlas.getFromTile(Tile.LAKE_LEFT);
        lakeBottomLeftTexture = atlas.getFromTile(Tile.LAKE_BOTTOM_LEFT);
        waterTexture = atlas.getFromTile(Tile.WATER);

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

        renderer.submit(transform1, reshiramTexture);

        renderer.submit(lakeBottomRightTransform, lakeBottomRightTexture);
        renderer.submit(lakeTopRightTransform, lakeTopRightTexture);
        renderer.submit(lakeTopLeftTransform, lakeTopLeftTexture);
        renderer.submit(lakeBottomLeftTransform, lakeBottomLeftTexture);

        for (Transform2D transform : lakeTopTransforms) {
            renderer.submit(transform, lakeTopTexture);
        }

        for (Transform2D transform : lakeBottomTransforms) {
            renderer.submit(transform, lakeBottomTexture);
        }

        for (Transform2D transform : lakeRightTransforms) {
            renderer.submit(transform, lakeRightTexture);
        }

        for (Transform2D transform : lakeLeftTransforms) {
            renderer.submit(transform, lakeLeftTexture);
        }

        for (Transform2D transform : waterTransforms) {
            renderer.submit(transform, waterTexture);
        }

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