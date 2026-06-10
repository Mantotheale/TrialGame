package com.game.renderer.texture.atlas;

import com.game.renderer.texture.Tile;
import com.game.util.HashingUtils;
import com.game.util.IOUtils;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AtlasLoader {
    private final Path atlasDirectory;
    private final Path checksumPath;
    private final Path metadataPath;

    public AtlasLoader(Path atlasDirectory) {
        this.atlasDirectory = atlasDirectory;
        this.checksumPath = atlasDirectory.resolve(AtlasGenerator.ATLASES_CHECKSUM_FILE);
        this.metadataPath = atlasDirectory.resolve(AtlasGenerator.ATLASES_METADATA_FILE);
    }

    public Optional<List<ImageAndMetadata>> loadAtlases(List<Tile> tiles) {
        if (!isIntegrityPreserved()) return Optional.empty();

        List<List<TileMetadata>> metadata = loadMetadata();
        if (!metadataMatchesRequestedTiles(metadata, tiles)) return Optional.empty();

        List<BufferedImage> images = loadImages(metadata.size());

        List<ImageAndMetadata> imageAndMetadata = new ArrayList<>();
        for (int i = 0; i < metadata.size(); i++) {
            imageAndMetadata.add(new ImageAndMetadata(images.get(i), metadata.get(i)));
        }

        return Optional.of(imageAndMetadata);
    }

    public boolean isIntegrityPreserved() {
        if (!Files.exists(checksumPath)) return false;

        String storedChecksum = IOUtils.readToString(checksumPath);
        String computedChecksum = computeChecksum();

        return storedChecksum.equals(computedChecksum);
    }

    private String computeChecksum() {
        Stream<byte[]> filesBytes = IOUtils.filesInDirectory(atlasDirectory).stream()
                .filter(p -> !p.equals(checksumPath))
                .sorted()
                .map(IOUtils::readAllBites);
        return HashingUtils.hash(filesBytes);
    }

    private List<List<TileMetadata>> loadMetadata() {
        List<List<TileMetadata>> metadata = new ArrayList<>();

        boolean foundAtlas = false;
        for (String line : IOUtils.readAllLines(metadataPath)) {
            if (line.isBlank()) continue;

            if (line.startsWith("atlas ")) {
                foundAtlas = true;
                metadata.add(new ArrayList<>());
            } else {
                if (!foundAtlas)
                    throw new RuntimeException("Tile data found before atlas header");

                String[] parts = line.split(" ");
                metadata.getLast().add(new TileMetadata(
                        Tile.valueOf(parts[0]),
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[3]),
                        Integer.parseInt(parts[4])
                ));
            }
        }

        return metadata;
    }

    private boolean metadataMatchesRequestedTiles(List<List<TileMetadata>> metadata, List<Tile> tiles) {
        Set<Tile> storedTiles = metadata.stream()
                .flatMap(List::stream)
                .map(TileMetadata::tile)
                .collect(Collectors.toSet());

        Set<Tile> requestedTiles = new HashSet<>(tiles);

        return storedTiles.equals(requestedTiles);
    }

    private List<BufferedImage> loadImages(int imagesCount) {
        List<BufferedImage> images = new ArrayList<>();
        for (int i = 1; i <= imagesCount; i++)
            images.add(IOUtils.loadImage(atlasDirectory.resolve("atlas_" + i + ".png")));
        return images;
    }
}
