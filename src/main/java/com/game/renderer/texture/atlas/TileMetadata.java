package com.game.renderer.texture.atlas;

import com.game.renderer.texture.Tile;

public record TileMetadata(Tile tile, int cornerX, int cornerY, int width, int height) { }