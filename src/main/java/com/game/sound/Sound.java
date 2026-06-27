package com.game.sound;

import java.nio.file.Path;

public enum Sound {
    HIT("collision.ogg"),
    NATIONAL_PARK("national_park_theme.ogg");

    private final static Path SOUNDS_BASE_PATH = Path.of("src/main/resources/sounds");

    private final String fileName;

    Sound(String fileName) {
        this.fileName = fileName;
    }

    public Path path() {
        return SOUNDS_BASE_PATH.resolve(fileName);
    }
}
