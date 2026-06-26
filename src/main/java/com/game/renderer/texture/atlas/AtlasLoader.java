package com.game.renderer.texture.atlas;

import com.game.renderer.texture.Tile;
import com.game.util.HashingUtils;
import com.game.util.FileUtils;

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

    public Optional<List<PathAndMetadata>> loadAtlases(List<Tile> tiles, int tilePadding) {
        if (!isIntegrityPreserved()) return Optional.empty();

        MetadataAndPadding metadataAndPadding;
        try {
            metadataAndPadding = loadMetadata();
        } catch (Exception e) {
            return Optional.empty();
        }
        if (metadataAndPadding.padding != tilePadding) return Optional.empty();

        List<List<TileMetadata>> metadata = metadataAndPadding.metadata;
        if (!metadataMatchesRequestedTiles(metadata, tiles)) return Optional.empty();

        List<PathAndMetadata> pathAndMetadata = new ArrayList<>();
        for (int i = 0; i < metadata.size(); i++) {
            Path atlasPath = atlasDirectory.resolve("atlas_" + (i + 1) + ".png");
            pathAndMetadata.add(new PathAndMetadata(atlasPath, metadata.get(i)));
        }

        return Optional.of(pathAndMetadata);
    }

    public boolean isIntegrityPreserved() {
        if (!Files.exists(checksumPath)) return false;

        String storedChecksum = FileUtils.readToString(checksumPath);
        String computedChecksum = computeChecksum();

        return storedChecksum.equals(computedChecksum);
    }

    private String computeChecksum() {
        Stream<byte[]> filesBytes = FileUtils.filesInDirectory(atlasDirectory).stream()
                .filter(p -> !p.equals(checksumPath))
                .sorted()
                .map(FileUtils::readAllBites);
        return HashingUtils.hash(filesBytes);
    }

    private MetadataAndPadding loadMetadata() {
        List<String> lines = FileUtils.readAllLines(metadataPath);

        if (!lines.getFirst().startsWith("padding"))
            throw new RuntimeException("Padding metadata not found");
        int padding = Integer.parseInt(lines.removeFirst().substring(8));

        List<List<TileMetadata>> metadata = new ArrayList<>();
        boolean foundAtlas = false;
        for (String line : lines) {
            if (line.isBlank()) continue;

            if (line.startsWith("atlas")) {
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

        return new MetadataAndPadding(metadata, padding);
    }

    private boolean metadataMatchesRequestedTiles(List<List<TileMetadata>> metadata, List<Tile> tiles) {
        Set<Tile> storedTiles = metadata.stream()
                .flatMap(List::stream)
                .map(TileMetadata::tile)
                .collect(Collectors.toSet());

        Set<Tile> requestedTiles = new HashSet<>(tiles);

        return storedTiles.equals(requestedTiles);
    }

    private record MetadataAndPadding(List<List<TileMetadata>> metadata, int padding) { }
}
