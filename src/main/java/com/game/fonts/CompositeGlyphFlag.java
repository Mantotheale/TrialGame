package com.game.fonts;

import org.jetbrains.annotations.NotNull;

public record CompositeGlyphFlag(short flagBytes) {
    public boolean arg1and2areWords() {
        return Mask.ARG_1_AND_2_ARE_WORDS.isSet(flagBytes);
    }

    public boolean argsAreXYValues() {
        return Mask.ARGS_ARE_XY_VALUES.isSet(flagBytes);
    }

    public boolean roundXYtoGrid() {
        return Mask.ROUND_XY_TO_GRID.isSet(flagBytes);
    }

    public boolean weHaveAScale() {
        return Mask.WE_HAVE_A_SCALE.isSet(flagBytes);
    }

    public boolean moreComponents() {
        return Mask.MORE_COMPONENTS.isSet(flagBytes);
    }

    public boolean weHaveAnXYScale() {
        return Mask.WE_HAVE_AN_X_AND_Y_SCALE.isSet(flagBytes);
    }

    public boolean weHaveA2by2() {
        return Mask.WE_HAVE_A_TWO_BY_TWO.isSet(flagBytes);
    }

    public boolean weHaveInstructions() {
        return Mask.WE_HAVE_INSTRUCTIONS.isSet(flagBytes);
    }

    public boolean useMyMetrics() {
        return Mask.USE_MY_METRICS.isSet(flagBytes);
    }

    public boolean overlapCompound() {
        return Mask.OVERLAP_COMPOUND.isSet(flagBytes);
    }

    public boolean scaledComponentOffset() {
        return Mask.SCALED_COMPONENT_OFFSET.isSet(flagBytes);
    }

    public boolean unscaledComponentOffset() {
        return Mask.UNSCALED_COMPONENT_OFFSET.isSet(flagBytes);
    }

    @Override
    public @NotNull String toString() {
        String binaryStr = Integer.toBinaryString(flagBytes & 0xFFFF);
        String paddedStr = "0".repeat(16 - binaryStr.length()) + binaryStr;

        return "Flag(" + paddedStr + ")";
    }

    private enum Mask {
        ARG_1_AND_2_ARE_WORDS((short) 0x0001),
        ARGS_ARE_XY_VALUES((short) 0x0002),
        ROUND_XY_TO_GRID((short) 0x0004),
        WE_HAVE_A_SCALE((short) 0x0008),
        MORE_COMPONENTS((short) 0x0020),
        WE_HAVE_AN_X_AND_Y_SCALE((short) 0x0040),
        WE_HAVE_A_TWO_BY_TWO((short) 0x0080),
        WE_HAVE_INSTRUCTIONS((short) 0x0100),
        USE_MY_METRICS((short) 0x0200),
        OVERLAP_COMPOUND((short) 0x0400),
        SCALED_COMPONENT_OFFSET((short) 0x0800),
        UNSCALED_COMPONENT_OFFSET((short) 0x1000);

        private final short mask;

        Mask(short mask) {
            this.mask = mask;
        }

        boolean isSet(short value) {
            return (mask & value) != 0;
        }
    }
}
