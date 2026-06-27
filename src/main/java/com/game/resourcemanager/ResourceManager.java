package com.game.resourcemanager;

import com.game.renderer.texture.*;
import com.game.renderer.texture.atlas.*;
import com.game.sound.Sound;
import com.game.sound.SoundBuffer;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceManager {
    public static final int MAX_ATLAS_SIZE = 1024;
    private static final TextureAttributes texAttr = new TextureAttributes.Builder()
            .magnifyingFilter(TextureMagnifyingFilter.NEAREST)
            .minifyingFilter(TextureMinifyingFilter.NEAREST)
            .mipmap(false)
            .build();

    private final TextureAtlas[] atlases;
    private final Map<Tile, Integer> tileAtlasMap;
    private final Map<Sound, SoundBuffer> sounds;

    public ResourceManager(Path atlasesPath, int tilesPadding) {
        List<Tile> requestedTiles = List.of(Tile.values());

        AtlasLoader atlasLoader = new AtlasLoader(atlasesPath);
        List<PathAndMetadata> atlasesMetadata = atlasLoader
                .loadAtlases(requestedTiles, tilesPadding)
                .orElseGet(() -> {
                    AtlasGenerator atlasGenerator = new AtlasGenerator(atlasesPath, MAX_ATLAS_SIZE);
                    atlasGenerator.generateAtlases(requestedTiles, tilesPadding);
                    return atlasLoader.loadAtlases(requestedTiles, tilesPadding).orElseThrow();
                });

        atlases = new TextureAtlas[atlasesMetadata.size()];
        tileAtlasMap = new EnumMap<>(Tile.class);

        for (int i = 0; i < atlasesMetadata.size(); i++) {
            PathAndMetadata pathAndMetadata = atlasesMetadata.get(i);
            Path path = pathAndMetadata.path();
            List<TileMetadata> metadata = pathAndMetadata.tilesMetadata();

            atlases[i] = new TextureAtlas(texAttr, path, metadata);
            for (TileMetadata m: metadata)
                tileAtlasMap.put(m.tile(), i);
        }

        sounds = new HashMap<>();
        for (Sound s: Sound.values())
            sounds.put(s, new SoundBuffer(s.path()));
    }

    public Texture getTexture(Tile tile) {
        return atlases[tileAtlasMap.get(tile)].getFromTile(tile);
    }

    public SoundBuffer getSoundBuffer(Sound sound) {
        SoundBuffer buffer = sounds.get(sound);

        if (buffer == null)
            throw new IllegalStateException("The requested sound is not in the resource manager. Requested " + sound);

        return buffer;
    }

    public void delete() {
        for (TextureAtlas a: atlases) {
            a.delete();
        }
    }
}
