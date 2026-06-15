package com.game.renderer.texture;

import java.nio.file.Path;

public enum Tile {
    RESHIRAM("reshiram.png"),
    MEWTWO("mewtwo.png"),
    GRASS("grass.png"),
    WATER("water.png"),
    LAKE_BOTTOM("lake_bottom.png"),
    LAKE_BOTTOM_RIGHT("lake_bottom_right.png"),
    LAKE_RIGHT("lake_right.png"),
    LAKE_TOP_RIGHT("lake_top_right.png"),
    LAKE_TOP("lake_top.png"),
    LAKE_TOP_LEFT("lake_top_left.png"),
    LAKE_LEFT("lake_left.png"),
    LAKE_BOTTOM_LEFT("lake_bottom_left.png"),
    LAKE_FULL("lake_full.png"),
    BROKEN_WALL("broken_wall.png"),
    WALLED_WATER("walled_water.png"),
    WALLED_WATER_SOURCE("walled_water_source.png"),;

    private final static Path TILES_BASE_PATH = Path.of("src/main/resources/tiles");

    private final String fileName;

    Tile(String fileName) {
        this.fileName = fileName;
    }

    public Path path() {
        return TILES_BASE_PATH.resolve(fileName);
    }
}
