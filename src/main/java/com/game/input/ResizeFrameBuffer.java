package com.game.input;

public record ResizeFrameBuffer(int width, int height) implements Input {
    public ResizeFrameBuffer {
        if (width <= 0)
            throw new IllegalArgumentException("Frame buffer width must be positive. Was " + width);

        if (height <= 0)
            throw new IllegalArgumentException("Frame buffer height must be positive. Was " + height);
    }
}
