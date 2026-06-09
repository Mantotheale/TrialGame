package com.game.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public final class IOUtils {
    private IOUtils() { }

    public static void saveStringToFile(Path path, String s) {
        try {
            Files.writeString(path, s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] readAllBites(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> readAllLines(Path path) {
        try {
            return Files.readAllLines(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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

    public static void deleteFile(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Stream<Path> filesInDirectory(Path path) {
        try {
            return Files.list(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
