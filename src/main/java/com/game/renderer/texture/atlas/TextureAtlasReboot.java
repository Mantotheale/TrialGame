package com.game.renderer.texture.atlas;

import com.game.util.Image;
import com.game.util.Pair;

import java.util.*;
import java.util.stream.IntStream;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

public class TextureAtlasReboot {
    private final static int ATLAS_MIN_SIZE = 32;
    private final static int ATLAS_MAX_SIZE = 1024;

    private final int glId;

    public TextureAtlasReboot() {
        this.glId = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, glId);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RGBA,
                ATLAS_MIN_SIZE,
                ATLAS_MIN_SIZE,
                0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                0
        );
    }

    private static void placeTiles(
            Image atlas,
            List<Image> tiles,
            List<PixelLocation> placements,
            int padding
    ) {
        for (int i = 0; i < tiles.size(); i++) {
            Image tile = tiles.get(i);
            PixelLocation p = placements.get(i);


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
            Image tile = tiles.get(idx);

            boolean fit = false;
            for (int i = 0; i < newEmptySpaces.size(); i++) {
                EmptySpace emptySpace = newEmptySpaces.get(i);
                if (emptySpace.canFit(tile, padding)) {
                    List<EmptySpace> resultingSpaces = emptySpace.splitOnInsertion(tile, padding);
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
        public boolean canFit(Image image, int padding) {
            return image.width() + padding * 2 <= width && image.height() + padding * 2 <= height;
        }

        public List<EmptySpace> splitOnInsertion(Image image, int padding) {
            if (!canFit(image, padding)) throw new IllegalStateException("Tile can't fit in the empty space");

            List<EmptySpace> resultingSpaces = new ArrayList<>();

            int doublePadding = padding * 2;
            if (this.width - image.width() > doublePadding) {
                EmptySpace right = new EmptySpace(
                        this.bottomLeft.addX(image.width() + doublePadding),
                        this.width - image.width() - doublePadding,
                        image.height() + doublePadding
                );
                resultingSpaces.add(right);
            }

            if (this.height - image.height() > doublePadding) {
                EmptySpace up = new EmptySpace(
                        this.bottomLeft.addY(image.height() + doublePadding),
                        this.width,
                        this.height - image.height() - doublePadding
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
}