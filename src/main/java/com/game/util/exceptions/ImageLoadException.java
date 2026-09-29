package com.game.util.exceptions;

import java.nio.file.Path;

public class ImageLoadException extends RuntimeException {
    public ImageLoadException(Path path, String reason) {
        super("Couldn't open image on path " + path + ". Reason: " + reason);
    }
}