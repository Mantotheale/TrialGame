package com.game.util.image;

public interface PixelSource {
    int width();
    int height();
    RGBA getPixel(int x, int y);
}
