package com.game.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
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

    public static String readToString(Path path) {
        try {
            return Files.readString(path);
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

    public static List<Path> filesInDirectory(Path path) {
        try (Stream<Path> files = Files.list(path)) {
            return files.toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deletePath(Path path) {
        try (Stream<Path> walk = Files.walk(path)) {
            deleteStream(walk);
        } catch (IOException e) {
            throw new RuntimeException("Failed to walk path: " + path, e);
        }
    }

    public static void deletePathContent(Path path) {
        try (Stream<Path> walk = Files.walk(path)) {
            deleteStream(walk.filter(p -> !p.equals(path)));
        } catch (IOException e) {
            throw new RuntimeException("Failed to walk path: " + path, e);
        }
    }

    public static void createDirectory(Path path) {
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void deleteStream(Stream<Path> paths) {
        paths.sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to delete element " + p, e);
                    }
                });
    }
}
