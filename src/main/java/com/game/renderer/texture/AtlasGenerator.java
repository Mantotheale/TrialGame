package com.game.renderer.texture;

import com.game.util.HashingUtils;
import com.game.util.IOUtils;
import com.game.util.Vec2i;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public final class AtlasGenerator {
    private final static int ATLAS_SIZE = 1024;
    private final static Path ATLASES_PATH = Path.of("src/main/resources/atlases");
    private final static String METADATA_FILE_NAME = "atlases_metadata.txt";

    private AtlasGenerator() { }

    public static void generateAtlases(List<Tile> tiles) {
        if (tiles.isEmpty()) throw new IllegalArgumentException("Can't build an atlas with no tiles");

        System.out.println("Loaded tiles are\n" + loadMetadata());

        List<TileImagePair> pairedTiles = openImages(tiles);
        pairedTiles.sort(AtlasGenerator::compareTiles);

        Map<Integer, List<TileMetadata>> tilesMetadata = new TreeMap<>();
        int atlasCount = 1;
        while (!pairedTiles.isEmpty()) {
            ImageAndMetadata atlas = packImages(pairedTiles);

            IOUtils.saveImage(ATLASES_PATH.resolve("atlas_" + atlasCount + ".png"), atlas.image);
            System.out.println("Created atlas " + atlasCount);
            System.out.println("With metadata: " + atlas.tileMetadata);

            tilesMetadata.put(atlasCount, atlas.tileMetadata);
            atlasCount++;
        }

        saveMetadata(tilesMetadata);
    }

    private static ImageAndMetadata packImages(List<TileImagePair> pairedTiles) {
        BufferedImage atlas = new BufferedImage(ATLAS_SIZE, ATLAS_SIZE, BufferedImage.TYPE_INT_ARGB);
        List<TileMetadata> metadata = new ArrayList<>();

        Graphics2D g = atlas.createGraphics();

        Vec2i pointer = new Vec2i(0, 0);
        int rowHeight = 0;

        while (!pairedTiles.isEmpty()) {
            TileImagePair pair =  pairedTiles.getFirst();
            int width = pair.image.getWidth();
            int height = pair.image.getHeight();

            if (pointer.x() + width > ATLAS_SIZE) {
                pointer = new Vec2i(0, pointer.y() + rowHeight);
                rowHeight = 0;
            }

            if (pointer.y() + height > ATLAS_SIZE) {
                g.dispose();
                return new ImageAndMetadata(atlas, metadata);
            }

            g.drawImage(pair.image, pointer.x(), pointer.y(), null);
            pairedTiles.removeFirst();
            metadata.add(new TileMetadata(pair.tile, pointer.x(), pointer.y(), width, height));

            pointer = new Vec2i(pointer.x() + width, pointer.y());
            rowHeight = Math.max(rowHeight, height);
        }

        g.dispose();
        return new  ImageAndMetadata(atlas, metadata);
    }

    private static List<TileImagePair> openImages(List<Tile> tiles) {
        List<TileImagePair> pairs = new ArrayList<>();
        for (Tile tile : tiles) {
            BufferedImage image = IOUtils.loadImage(tile.path());

            if (image.getWidth() > ATLAS_SIZE || image.getHeight() > ATLAS_SIZE)
                throw new IllegalArgumentException("Can't open an image with size " + image.getWidth() + " " + image.getHeight() + ". The atlas size is " + ATLAS_SIZE);

            pairs.add(new TileImagePair(tile, image));
        }
        return pairs;
    }

    private static void saveMetadata(Map<Integer, List<TileMetadata>> tilesMetadata) {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<Integer, List<TileMetadata>> entry : tilesMetadata.entrySet()) {
            sb.append("atlas ").append(entry.getKey()).append('\n');

            for (TileMetadata metadata : entry.getValue()) {
                sb.append(metadata.tile().name())
                        .append(' ').append(metadata.cornerX())
                        .append(' ').append(metadata.cornerY())
                        .append(' ').append(metadata.width())
                        .append(' ').append(metadata.height())
                        .append('\n');
            }
        }

        Stream<byte[]> atlasesBytes = tilesMetadata.keySet().stream()
                .map(idx -> ATLASES_PATH.resolve("atlas_" + idx + ".png"))
                .map(IOUtils::readAllBites);

        String hash = HashingUtils.hash(Stream.concat(Stream.of(sb.toString().getBytes()), atlasesBytes));

        IOUtils.saveStringToFile(
                ATLASES_PATH.resolve(METADATA_FILE_NAME),
                sb.insert(0, hash + '\n').toString()
        );

        IOUtils.filesInDirectory(ATLASES_PATH)
                .filter(p -> isStaleAtlasFile(p, tilesMetadata.size()))
                .forEach(IOUtils::deleteFile);
    }

    private static Map<Integer, List<TileMetadata>> loadMetadata() {
        if (!checkMetadataIntegrity()) {
            throw new IllegalStateException("Metadata integrity check failed");
        }

        List<String> lines = IOUtils.readAllLines(ATLASES_PATH.resolve(METADATA_FILE_NAME));
        lines.removeFirst();

        Map<Integer, List<TileMetadata>> result = new TreeMap<>();

        int currentAtlas = -1;
        for (String line : lines) {
            if (line.isBlank()) continue;

            if (line.startsWith("atlas ")) {
                currentAtlas = Integer.parseInt(line.substring(6).trim());
                result.put(currentAtlas, new ArrayList<>());
            } else {
                if (currentAtlas == -1)
                    throw new RuntimeException("Tile data found before atlas header");

                String[] parts = line.split(" ");
                result.get(currentAtlas).add(new TileMetadata(
                        Tile.valueOf(parts[0]),
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[3]),
                        Integer.parseInt(parts[4])
                ));
            }
        }

        return result;
    }

    private static boolean checkMetadataIntegrity() {
        byte[] fileBytes = IOUtils.readAllBites(ATLASES_PATH.resolve(METADATA_FILE_NAME));
        String fileContent = new String(fileBytes);

        int firstNewline = fileContent.indexOf('\n');
        if (firstNewline == -1) throw new RuntimeException("Metadata file is missing hash line");


        Stream<byte[]> metadataBytes = Stream.of(Arrays.copyOfRange(fileBytes, firstNewline + 1, fileBytes.length));

        Stream<byte[]> atlasesBytes = IOUtils.filesInDirectory(ATLASES_PATH)
                .filter(p -> !p.getFileName().toString().equals(METADATA_FILE_NAME))
                .sorted()
                .map(IOUtils::readAllBites);

        String computedHash = HashingUtils.hash(Stream.concat(metadataBytes, atlasesBytes));
        String storedHash = fileContent.substring(0, firstNewline);

        System.out.println("Metadata hash: " + computedHash);
        System.out.println("Metadata stored hash: " + storedHash);
        return computedHash.equals(storedHash);
    }

    private static boolean isStaleAtlasFile(Path path, int atlasCount) {
        String name = path.getFileName().toString();
        if (name.equals(METADATA_FILE_NAME)) return false;

        if (name.startsWith("atlas_") && name.endsWith(".png")) {
            try {
                int index = Integer.parseInt(name.substring(6, name.length() - 4));
                return index > atlasCount;
            } catch (NumberFormatException e) {
                return true;
            }
        }

        return true;
    }

    private static int compareTiles(TileImagePair a, TileImagePair b) {
        int heightComparison = Integer.compare(b.image.getHeight(), a.image.getHeight());
        if (heightComparison != 0) return heightComparison;
        return Integer.compare(b.image.getWidth(), a.image.getWidth());
    }

    private record TileImagePair(Tile tile, BufferedImage image) { }

    private record TileMetadata(Tile tile, int cornerX, int cornerY, int width, int height) { }

    private record ImageAndMetadata(BufferedImage image, List<TileMetadata> tileMetadata) { }
}
