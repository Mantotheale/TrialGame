package com.game.renderer.texture;

import com.game.util.IOUtils;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TextureAtlas {
    public TextureAtlas(List<Tile> tiles) {
        List<TileImagePair> pairedTiles = openImages(tiles);
        pairedTiles.sort(TextureAtlas::compareTiles);

        int atlasSize = 16;

        while (atlasSize <= 1024) {
            Optional<BufferedImage> atlas = packImages(pairedTiles, atlasSize);

            if (atlas.isPresent()) {
                System.out.println("Managed to create the atlas with size " + atlasSize);
                IOUtils.saveImage(Path.of("src/main/resources/atlases/atlas.png"), atlas.get());
                return;
            }

            atlasSize *= 2;
        }
    }

    private static Optional<BufferedImage> packImages(List<TileImagePair> pairedTiles, int size) {
        BufferedImage atlas = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        int pointerX = 0;
        int pointerY = 0;
        int nextY = 0;

        for (TileImagePair tileImage : pairedTiles) {
            BufferedImage image = tileImage.image;
            int width = image.getWidth();
            int height = image.getHeight();

            if (size - pointerX < width || size - pointerY < height) {
                pointerX = 0;
                pointerY = nextY;

                if (size - pointerX < width || size - pointerY < height) {
                    return Optional.empty();
                }
            }

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    atlas.setRGB(pointerX + i, pointerY + j, image.getRGB(i, j));
                }
            }

            if (pointerY + height > nextY) {
                nextY = pointerY + height;
            }

            pointerX += width;
        }

        return Optional.of(atlas);
    }

    private static List<TileImagePair> openImages(List<Tile> tiles) {
        List<TileImagePair> pairs = new ArrayList<>();
        for (Tile tile : tiles)
                pairs.add(new TileImagePair(tile, IOUtils.loadImage(tile.path())));
        return pairs;
    }

    private static int compareTiles(TileImagePair a, TileImagePair b) {
        int heightComparison = Integer.compare(b.image.getHeight(), a.image.getHeight());
        if (heightComparison != 0) return heightComparison;
        return Integer.compare(b.image.getWidth(), a.image.getWidth());
    }

    private record TileImagePair(Tile tile, BufferedImage image) { }
}
