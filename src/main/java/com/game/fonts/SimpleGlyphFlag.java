package com.game.fonts;

import org.jetbrains.annotations.NotNull;

record SimpleGlyphFlag(byte flagByte) {
    public boolean onCurve() {
        return Mask.ON_CURVE_POINT.isSet(flagByte);
    }

    public boolean xShortVector() {
        return Mask.X_SHORT_VECTOR.isSet(flagByte);
    }

    public boolean yShortVector() {
        return Mask.Y_SHORT_VECTOR.isSet(flagByte);
    }

    public boolean repeat() {
        return Mask.REPEAT_FLAG.isSet(flagByte);
    }

    public boolean isSameOrPositiveXShortVector() {
        return Mask.X_IS_SAME_OR_POSITIVE_X_SHORT_VECTOR.isSet(flagByte);
    }

    public boolean isSameOrPositiveYShortVector() {
        return Mask.Y_IS_SAME_OR_POSITIVE_Y_SHORT_VECTOR.isSet(flagByte);
    }

    public boolean overlapSimple() {
        return Mask.OVERLAP_SIMPLE.isSet(flagByte);
    }

    public SimpleGlyphFlag toNonRepeat() {
        if (!repeat()) return this;
        return new SimpleGlyphFlag(Mask.REPEAT_FLAG.flipOff(flagByte));
    }

    @Override
    public @NotNull String toString() {
        String binaryStr = Integer.toBinaryString(flagByte & 0xFF);
        String paddedStr = "0".repeat(8 - binaryStr.length()) + binaryStr;

        return "Flag(" + paddedStr + ")";
    }

    private enum Mask {
        ON_CURVE_POINT((byte) 0x01),
        X_SHORT_VECTOR((byte) 0x02),
        Y_SHORT_VECTOR((byte) 0x04),
        REPEAT_FLAG((byte) 0x08),
        X_IS_SAME_OR_POSITIVE_X_SHORT_VECTOR((byte) 0x10),
        Y_IS_SAME_OR_POSITIVE_Y_SHORT_VECTOR((byte) 0x20),
        OVERLAP_SIMPLE((byte) 0x40);

        private final byte mask;

        Mask(byte mask) {
            this.mask = mask;
        }

        boolean isSet(byte value) {
            return (mask & value) != 0;
        }

        byte flipOff(byte value) {
            return (byte) (~mask & value);
        }
    }
}
