package com.game.fonts;

import org.jetbrains.annotations.NotNull;

public record Contour(short offset, short end, short length) {
    public static Contour firstContour(short end) {
        return new Contour((short) 0, end, (short) (Short.toUnsignedInt(end) + 1));
    }

    public static Contour fromPreviousContour(Contour previous, short end) {
        int previousEnd = Short.toUnsignedInt(previous.end);
        short offset = (short) (previousEnd + 1);
        short length = (short) (Short.toUnsignedInt(end) - previousEnd);

        return new Contour(offset, end, length);
    }

    @Override
    public @NotNull String toString() {
        return "Contour(" +
                "offset: " + Short.toUnsignedInt(offset) + ", " +
                "end: " + Short.toUnsignedInt(end) + ", " +
                "length: " + Short.toUnsignedInt(length) +
                ")";
    }
}
