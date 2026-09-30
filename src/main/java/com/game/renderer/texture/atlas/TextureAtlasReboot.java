package com.game.renderer.texture.atlas;

import com.game.math.Vec2f;
import com.game.util.image.Image;
import com.game.util.Pair;
import com.game.util.image.PaddedPixelSource;
import com.game.util.image.PixelSource;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

public class TextureAtlasReboot {
    private final static int ATLAS_MIN_SIZE = 32;
    private final static int ATLAS_MAX_SIZE = 1024;
    private final static int TILE_PADDING = 1;

    private final int glId;
    private int size;
    private final List<ModifiableTextureHandle> textureHandles;
    private List<EmptySpace> emptySpaces;

    public TextureAtlasReboot() {
        this.glId = glGenTextures();
        this.size = ATLAS_MIN_SIZE;
        this.textureHandles = new ArrayList<>();
        this.emptySpaces = new ArrayList<>();
        emptySpaces.add(new EmptySpace(new PixelLocation(0, 0), ATLAS_MIN_SIZE, ATLAS_MIN_SIZE));

        glBindTexture(GL_TEXTURE_2D, glId);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RGBA8,
                ATLAS_MIN_SIZE,
                ATLAS_MIN_SIZE,
                0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                0
        );
    }

    public Optional<TextureHandle> addTile(Image tile) {
        return addTiles(List.of(tile)).map(List::getFirst);
    }

    public Optional<List<TextureHandle>> addTiles(List<Image> tiles) {
        var placementResult = generatePlacements(tiles, emptySpaces, TILE_PADDING);
        if (placementResult.isEmpty()) return Optional.empty();
        var pair = placementResult.get();
        List<PixelLocation> placements = pair.first();
        List<EmptySpace> newEmptySpaces = pair.second();

        uploadTiles(glId, tiles, placements, TILE_PADDING);
        this.emptySpaces = newEmptySpaces;
        List<ModifiableTextureHandle> newTextureHandles = Stream.generate(ModifiableTextureHandle::new).limit(tiles.size()).toList();
        updateHandles(newTextureHandles, tiles, placements, glId, TILE_PADDING, size);
        this.textureHandles.addAll(newTextureHandles);

        return Optional.of(Collections.unmodifiableList(newTextureHandles));
    }

    private static void updateHandles(
            List<ModifiableTextureHandle> handles,
            List<Image> tiles,
            List<PixelLocation> placements,
            int textureId,
            int padding,
            int atlasSize
    ) {
        for (int i = 0; i < handles.size(); i++) {
            ModifiableTextureHandle textureHandle = handles.get(i);
            Image tile = tiles.get(i);
            PixelLocation placement = placements.get(i);

            textureHandle.update(
                    textureId,
                    placement.addX(padding).addY(padding),
                    tile.width(),
                    tile.height(),
                    atlasSize
            );
        }
    }

    private static void uploadTiles(
            int glTextureId,
            List<Image> tiles,
            List<PixelLocation> placements,
            int padding
    ) {
        glBindTexture(GL_TEXTURE_2D, glTextureId);

        for (int i = 0; i < tiles.size(); i++)
            uploadTile(tiles.get(i), placements.get(i), padding);
    }

    private static void uploadTile(Image tile, PixelLocation placement, int padding) {
        try (Image paddedTile = Image.copy(new PaddedPixelSource(tile, padding))) {
            glTexSubImage2D(
                    GL_TEXTURE_2D,
                    0,
                    placement.x(),
                    placement.y(),
                    paddedTile.width(),
                    paddedTile.height(),
                    GL_RGBA,
                    GL_UNSIGNED_BYTE,
                    paddedTile.asRawBuffer()
            );
        }
    }

    private static Optional<Pair<List<PixelLocation>, List<EmptySpace>>> generatePlacements(
            List<Image> tiles,
            List<EmptySpace> emptySpaces,
            int padding
    ) {
        List<Integer> order = IntStream.range(0, tiles.size())
                .boxed()
                .sorted(
                        Comparator.comparingInt((Integer i) -> tiles.get(i).height())
                                .thenComparingInt((Integer i) -> tiles.get(i).width())
                                .reversed()
                )
                .toList();

        PixelLocation[] placements = new PixelLocation[tiles.size()];
        List<EmptySpace> newEmptySpaces = new ArrayList<>(emptySpaces);

        for (int idx: order) {
            PixelSource tile = new PaddedPixelSource(tiles.get(idx), padding);

            boolean fit = false;
            for (int i = 0; i < newEmptySpaces.size(); i++) {
                EmptySpace emptySpace = newEmptySpaces.get(i);
                if (emptySpace.canFit(tile)) {
                    List<EmptySpace> resultingSpaces = emptySpace.splitOnInsertion(tile);
                    newEmptySpaces.remove(i);
                    newEmptySpaces.addAll(i, resultingSpaces);
                    placements[idx] = emptySpace.bottomLeft;
                    fit = true;
                    break;
                }
            }

            if (!fit) return  Optional.empty();
        }

        return Optional.of(new Pair<>(Arrays.stream(placements).toList(), newEmptySpaces));
    }

    private record EmptySpace(PixelLocation bottomLeft, int width, int height) {
        public boolean canFit(PixelSource image) {
            return image.width() <= width && image.height() <= height;
        }

        public List<EmptySpace> splitOnInsertion(PixelSource image) {
            if (!canFit(image)) throw new IllegalStateException("Tile can't fit in the empty space");

            List<EmptySpace> resultingSpaces = new ArrayList<>();

            if (this.width > image.width()) {
                EmptySpace right = new EmptySpace(
                        this.bottomLeft.addX(image.width()),
                        this.width - image.width(),
                        image.height()
                );
                resultingSpaces.add(right);
            }

            if (this.height > image.height()) {
                EmptySpace up = new EmptySpace(
                        this.bottomLeft.addY(image.height()),
                        this.width,
                        this.height - image.height()
                );
                resultingSpaces.add(up);
            }

            return resultingSpaces;
        }
    }

    private record PixelLocation(int x, int y) {
        public PixelLocation addX(int x) {
            return new PixelLocation(this.x + x, this.y);
        }

        public PixelLocation addY(int y) {
            return new PixelLocation(this.x, this.y + y);
        }
    }

    private static class ModifiableTextureHandle implements TextureHandle {
        private int textureId;
        Vec2f bottomLeft;
        Vec2f bottomRight;
        Vec2f topRight;
        Vec2f topLeft;

        private ModifiableTextureHandle() {
            textureId = 0;
            bottomLeft = null;
            bottomRight = null;
            topRight = null;
            topLeft = null;
        }

        private ModifiableTextureHandle(int textureId, PixelLocation bottomLeft, int bitWidth, int bitHeight, int atlasSize) {
            update(textureId, bottomLeft, bitWidth, bitHeight, atlasSize);
        }

        private void update(int textureId, PixelLocation bottomLeft, int bitWidth, int bitHeight, int atlasSize) {
            this.textureId = textureId;

            float bottomLeftX = (float) bottomLeft.x() / (float) atlasSize;
            float bottomLeftY = (float) bottomLeft.y() / (float) atlasSize;
            float width = (float) bitWidth / (float) atlasSize;
            float height = (float) bitHeight / (float) atlasSize;

            this.bottomLeft = new Vec2f(bottomLeftX, bottomLeftY);
            this.bottomRight = new Vec2f(bottomLeftX + width, bottomLeftY);
            this.topRight = new Vec2f(bottomLeftX + width, bottomLeftY + height);
            this.topLeft = new Vec2f(bottomLeftX, bottomLeftY + height);
        }

        @Override
        public int texId() {
            return textureId;
        }

        @Override
        public Vec2f bottomLeft() {
            return bottomLeft;
        }

        @Override
        public Vec2f bottomRight() {
            return bottomRight;
        }

        @Override
        public Vec2f topRight() {
            return topRight;
        }

        @Override
        public Vec2f topLeft() {
            return topLeft;
        }
    }

}