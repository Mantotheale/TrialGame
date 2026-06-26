package com.game.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class ImageUtils {
    private ImageUtils() { }

    public static BufferedImage loadImage(Path path) {
        File file = new File(path.toUri());

        try {
            return ImageIO.read(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveImage(BufferedImage image, Path path) {
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

    public static BufferedImage padImage(BufferedImage image, int padPixels) {
        int width = image.getWidth();
        int height = image.getHeight();
        int paddedWidth = width + 2 * padPixels;
        int paddedHeight = height + 2 * padPixels;
        BufferedImage paddedImage = new BufferedImage(paddedWidth, paddedHeight, image.getType());

        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                paddedImage.setRGB(padPixels + x, padPixels + y, image.getRGB(x, y));

        for (int i = 0; i < padPixels; i++)
            for (int x = 0; x < width; x++) {
                paddedImage.setRGB(padPixels + x, i, image.getRGB(x, 0));
                paddedImage.setRGB(padPixels + x, padPixels + height + i, image.getRGB(x, height - 1));
            }

        for (int i = 0; i < padPixels; i++)
            for (int y = 0; y < height; y++) {
                paddedImage.setRGB(i, padPixels + y, image.getRGB(0, y));
                paddedImage.setRGB(padPixels + width + i, padPixels + y, image.getRGB(width - 1, y));
            }

        for (int i = 0; i < padPixels; i++)
            for (int j = 0; j < padPixels; j++) {
                paddedImage.setRGB(i, j, image.getRGB(0, 0));
                paddedImage.setRGB(padPixels + width + i, j, image.getRGB(width - 1, 0));
                paddedImage.setRGB(i, padPixels + height + j, image.getRGB(0, height - 1));
                paddedImage.setRGB(padPixels + width + i, padPixels + height + j, image.getRGB(width - 1, height - 1));
            }

        return paddedImage;
    }
}
