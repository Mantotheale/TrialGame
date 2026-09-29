package com.game.util.image;

import org.jetbrains.annotations.NotNull;

public record RGBA(byte bitsR, byte bitsG, byte bitsB, byte bitsA) {
    public static RGBA fromRGBA(int r, int g, int b, int a) {
        if (r < 0 || g < 0 || b < 0 || a < 0 || r > 255 || g > 255 || b > 255 || a > 255)
            throw new IllegalArgumentException("The RGBA channels should be between 0 and 255. They were " + r + ", " + g + ", " + b + ", " + a);

        return new RGBA((byte) r, (byte) g, (byte) b, (byte) a);
    }

    public int r() {
        return Byte.toUnsignedInt(bitsR);
    }

    public int g() {
        return Byte.toUnsignedInt(bitsG);
    }

    public int b() {
        return Byte.toUnsignedInt(bitsB);
    }

    public int a() {
        return Byte.toUnsignedInt(bitsA);
    }

    @Override
    public @NotNull String toString() {
        return "RGBA(" + r() + ", " + g() + ", " + b() + ", " + a() + ")";
    }
}