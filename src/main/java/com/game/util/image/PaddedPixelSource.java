package com.game.util.image;

import com.game.util.exceptions.PixelOutOfBoundsException;

public class PaddedPixelSource implements PixelSource {
    private final PixelSource source;
    private final int padding;

    public PaddedPixelSource(PixelSource source, int padding) {
        if (padding < 0) throw new IllegalArgumentException("Padding must be non negative");

        this.source = source;
        this.padding = padding;
    }

    @Override
    public int width() {
        return source.width() + this.padding * 2;
    }

    @Override
    public int height() {
        return source.height() + this.padding * 2;
    }

    @Override
    public RGBA getPixel(int x, int y) {
        if (x < 0 || x >= width() || y < 0 || y >= height())
            throw new PixelOutOfBoundsException(x, y, width(), height());

        int imageX = Math.clamp(x - padding, 0, source.width() - 1);
        int imageY = Math.clamp(y - padding, 0, source.height() - 1);

        return source.getPixel(imageX, imageY);
    }
}
