package com.game.renderer.texture.atlas;

import com.game.renderer.texture.Tile;
import com.game.util.HashingUtils;
import com.game.util.IOUtils;
import com.game.util.Vec2i;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class AtlasGenerator {
    public static final String ATLASES_METADATA_FILE = "atlases_metadata.txt";
    public static final String ATLASES_CHECKSUM_FILE = "atlases_checksum.txt";

    private final Path atlasDirectory;
    private final int atlasSize;

    public AtlasGenerator(Path atlasDirectory, int atlasSize) {
        this.atlasDirectory = atlasDirectory;
        this.atlasSize = atlasSize;
    }

    public void generateAtlases(List<Tile> tiles) {
        if (tiles.isEmpty()) throw new IllegalArgumentException("Can't build an atlas with no tiles");

        IOUtils.deletePathContent(atlasDirectory);

        List<TileWithImage> tilesWithImages = openImages(tiles);
        List<List<TileMetadata>> atlasesMetadata = createAtlases(tilesWithImages);
        saveMetadata(atlasesMetadata);
        saveChecksum();
    }

    private List<TileWithImage> openImages(List<Tile> tiles) {
        List<TileWithImage> pairs = new ArrayList<>();

        for (Tile tile : tiles) {
            BufferedImage image = IOUtils.loadImage(tile.path());

            if (image.getWidth() > atlasSize || image.getHeight() > atlasSize)
                throw new IllegalArgumentException(
                        "Can't open an image with size " + image.getWidth() + "x" + image.getHeight() +
                                ". The atlas size is " + atlasSize);

            pairs.add(new TileWithImage(tile, image));
        }

        return pairs;
    }

    private List<List<TileMetadata>> createAtlases(List<TileWithImage> tilesWithImages) {
        tilesWithImages.sort(TileWithImage::compareTo);

        List<List<TileMetadata>> tilesMetadata = new ArrayList<>();

        while (!tilesWithImages.isEmpty()) {
            ImageAndMetadata atlasAndMetadata = packImages(tilesWithImages);

            Path imagePath = atlasDirectory.resolve("atlas_" + (tilesMetadata.size() + 1) + ".png");
            IOUtils.saveImage(imagePath, atlasAndMetadata.image());

            System.out.println("Created atlas " + (tilesMetadata.size() + 1));
            System.out.println("With metadata: " + atlasAndMetadata.tileMetadata());

            tilesMetadata.add(atlasAndMetadata.tileMetadata());
        }

        return tilesMetadata;
    }

    private ImageAndMetadata packImages(List<TileWithImage> tilesWithImages) {
        BufferedImage atlas = new BufferedImage(atlasSize, atlasSize, BufferedImage.TYPE_INT_ARGB);
        List<TileMetadata> metadata = new ArrayList<>();

        Graphics2D g = atlas.createGraphics();

        Vec2i pointer = new Vec2i(0, 0);
        int rowHeight = 0;
        while (!tilesWithImages.isEmpty()) {
            BufferedImage image = tilesWithImages.getFirst().image;
            int width = image.getWidth();
            int height = image.getHeight();

            if (pointer.x() + width > atlasSize) {
                pointer = new Vec2i(0, pointer.y() + rowHeight);
                rowHeight = 0;
            }

            if (pointer.y() + height > atlasSize) {
                g.dispose();
                return new ImageAndMetadata(atlas, metadata);
            }

            g.drawImage(image, pointer.x(), pointer.y(), null);
            Tile tile = tilesWithImages.removeFirst().tile;
            metadata.add(new TileMetadata(tile, pointer.x(), pointer.y(), width, height));

            pointer = new Vec2i(pointer.x() + width, pointer.y());
            rowHeight = Math.max(rowHeight, height);
        }

        g.dispose();
        return new ImageAndMetadata(atlas, metadata);
    }

    private void saveMetadata(List<List<TileMetadata>> atlasesMetadata) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i != atlasesMetadata.size(); i++) {
            List<TileMetadata> tilesMetadata = atlasesMetadata.get(i);

            sb.append("atlas ").append(i).append('\n');
            for (TileMetadata metadata : tilesMetadata) {
                sb.append(metadata.tile().name())
                        .append(' ').append(metadata.cornerX())
                        .append(' ').append(metadata.cornerY())
                        .append(' ').append(metadata.width())
                        .append(' ').append(metadata.height())
                        .append('\n');
            }
        }

        IOUtils.saveStringToFile(atlasDirectory.resolve(ATLASES_METADATA_FILE), sb.toString());
    }

    private void saveChecksum() {
        Stream<byte[]> filesBytes = IOUtils.filesInDirectory(atlasDirectory).stream()
                .sorted()
                .map(IOUtils::readAllBites);
        String hash = HashingUtils.hash(filesBytes);

        IOUtils.saveStringToFile(atlasDirectory.resolve(ATLASES_CHECKSUM_FILE), hash);
    }

    private record TileWithImage(Tile tile, BufferedImage image) implements Comparable<TileWithImage> {
        @Override
        public int compareTo(TileWithImage o) {
            int heightComparison = Integer.compare(o.image.getHeight(), this.image.getHeight());
            if (heightComparison != 0) return heightComparison;
            return Integer.compare(o.image.getWidth(), this.image.getWidth());
        }
    }
}
