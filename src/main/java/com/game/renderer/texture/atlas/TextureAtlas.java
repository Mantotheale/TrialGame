package com.game.renderer.texture.atlas;

import com.game.renderer.texture.SimpleTexture;
import com.game.renderer.texture.Texture;
import com.game.renderer.texture.TextureAttributes;
import com.game.renderer.texture.Tile;
import com.game.math.Vec2f;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;

public class TextureAtlas implements Texture {
    private final SimpleTexture innerTexture;
    private final EnumMap<Tile, Texture> subTextures;

    public TextureAtlas(TextureAttributes attributes, Path path, List<TileMetadata> tilesMetadata) {
        innerTexture = new SimpleTexture(attributes, path);

        subTextures = new EnumMap<>(Tile.class);
        tilesMetadata.forEach(
                m -> subTextures.put(
                        m.tile(),
                        new AtlasSubTexture(
                                innerTexture.texId(),
                                m.width(),
                                m.height(),
                                (float) m.width() / innerTexture.bitWidth(),
                                (float) m.height() / innerTexture.bitHeight(),
                                new Vec2f((float) m.cornerX() / innerTexture.bitWidth(), (float) m.cornerY() / innerTexture.bitHeight()),
                                new Vec2f((float) (m.cornerX() + m.width()) / innerTexture.bitWidth(), (float) m.cornerY() / innerTexture.bitHeight()),
                                new Vec2f((float) (m.cornerX() + m.width()) / innerTexture.bitWidth(), (float) (m.cornerY() + m.height()) / innerTexture.bitHeight()),
                                new Vec2f((float) m.cornerX() / innerTexture.bitWidth(), (float) (m.cornerY() + m.height()) / innerTexture.bitHeight())

                        )
                )
        );
    }

    public boolean isTileContained(Tile tile) {
        return subTextures.containsKey(tile);
    }

    public Texture getFromTile(Tile tile) {
        return subTextures.get(tile);
    }

    @Override
    public int texId() {
        return innerTexture.texId();
    }

    @Override
    public int bitWidth() {
        return innerTexture.bitWidth();
    }

    @Override
    public int bitHeight() {
        return innerTexture.bitHeight();
    }

    @Override
    public float normalizedWidth() {
        return innerTexture.normalizedWidth();
    }

    @Override
    public float normalizedHeight() {
        return innerTexture.normalizedHeight();
    }

    @Override
    public Vec2f bottomLeft() {
        return innerTexture.bottomLeft();
    }

    @Override
    public Vec2f bottomRight() {
        return innerTexture.bottomRight();
    }

    @Override
    public Vec2f topRight() {
        return innerTexture.topRight();
    }

    @Override
    public Vec2f topLeft() {
        return innerTexture.topLeft();
    }

    @Override
    public void delete() {
        innerTexture.delete();
    }

    private record AtlasSubTexture(
            int texId,
            int bitWidth,
            int bitHeight,
            float normalizedWidth,
            float normalizedHeight,
            Vec2f bottomLeft,
            Vec2f bottomRight,
            Vec2f topRight,
            Vec2f topLeft
            ) implements Texture {
        @Override
        public void delete() { }
    }
}
