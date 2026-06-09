package com.game.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public final class IOUtils {
    private IOUtils() { }

    public static BufferedImage loadImage(Path path) {
        File file = new File(path.toUri());

        try {
            return ImageIO.read(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveImage(Path path, BufferedImage image) {
        File outputFile = new File(path.toUri());

        boolean success;
        try {
            success = ImageIO.write(image, "png", outputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (!success)
            throw new RuntimeException("Couldn't save the image");
    }
}
