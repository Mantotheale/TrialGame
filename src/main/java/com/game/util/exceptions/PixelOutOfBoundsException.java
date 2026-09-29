package com.game.util.exceptions;

public class PixelOutOfBoundsException extends IndexOutOfBoundsException {
    public PixelOutOfBoundsException(int x, int y, int width, int height) {
        super("Pixel (" + x + ", " + y + ") out of bounds of size " + width + "x" + height);
    }
}
