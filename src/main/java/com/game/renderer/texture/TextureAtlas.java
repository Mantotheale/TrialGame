package com.game.renderer.texture;

import com.game.util.IOUtils;
import com.game.util.Vec2i;

import java.awt.*;
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
        Graphics2D g = atlas.createGraphics();

        int pointerX = 0;
        int pointerY = 0;
        List<Vec2i> rowCheckpoints = new ArrayList<>();

        for (TileImagePair tileImage : pairedTiles) {
            BufferedImage image = tileImage.image;
            int width = image.getWidth();
            int height = image.getHeight();


            while((!rowCheckpoints.isEmpty() && rowCheckpoints.getLast().y() - pointerY < height) ||  size - pointerX < width || size - pointerY < height) {
                if (rowCheckpoints.isEmpty()) {
                    g.dispose();
                    return Optional.empty();
                }

                Vec2i checkpoint = rowCheckpoints.removeLast();
                pointerY = checkpoint.y();

                if (rowCheckpoints.isEmpty()) {
                    pointerX = 0;
                } else  {
                    pointerX = rowCheckpoints.getLast().x();
                }
            }

            g.drawImage(image, pointerX, pointerY, null);

            if (!rowCheckpoints.isEmpty() && rowCheckpoints.getLast().y() == pointerY) {
                rowCheckpoints.removeLast();
            }

            pointerX += width;
            rowCheckpoints.add(new Vec2i(pointerX, pointerY + height));
        }

        g.dispose();
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
