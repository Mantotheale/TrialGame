package com.game.fonts;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

record MaxpTable(
        short majorVersion,
        short minorVersion,
        short numGlyphs,
        short maxPoints,
        short maxContours,
        short maxCompositePoints,
        short maxCompositeContours,
        short maxZones,
        short maxTwilightPoints,
        short maxStorage,
        short maxFunctionDefs,
        short maxInstructionDefs,
        short maxStackElements,
        short maxSizeOfInstructions,
        short maxComponentElements,
        short maxComponentDepth
) {
    public static MaxpTable fromChannel(ByteChannel channel, ByteBuffer u16Buffer) {
        return new MaxpTable(
                (short) 1,
                (short) 0,
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract16Bit(channel, u16Buffer)
        );
    }

    @Override
    public @NotNull String toString() {
        return "MaxpTable(" +
                "majorVersion: " + Short.toUnsignedInt(majorVersion) + ", " +
                "minorVersion: " + Short.toUnsignedInt(minorVersion) + ", " +
                "numGlyphs: " + Short.toUnsignedInt(numGlyphs) + ", " +
                "maxPoints: " + Short.toUnsignedInt(maxPoints) + ", " +
                "maxContours: " + Short.toUnsignedInt(maxContours) + ", " +
                "maxCompositePoints: " + Short.toUnsignedInt(maxCompositePoints) + ", " +
                "maxCompositeContours: " + Short.toUnsignedInt(maxCompositeContours) + ", " +
                "maxZones: " + Short.toUnsignedInt(maxZones) + ", " +
                "maxTwilightPoints: " + Short.toUnsignedInt(maxTwilightPoints) + ", " +
                "maxStorage: " + Short.toUnsignedInt(maxStorage) + ", " +
                "maxFunctionDefs: " + Short.toUnsignedInt(maxFunctionDefs) + ", " +
                "maxInstructionDefs: " + Short.toUnsignedInt(maxInstructionDefs) + ", " +
                "maxStackElements: " + Short.toUnsignedInt(maxStackElements) + ", " +
                "maxSizeOfInstructions: " + Short.toUnsignedInt(maxSizeOfInstructions) + ", " +
                "maxComponentElements: " + Short.toUnsignedInt(maxComponentElements) + ", " +
                "maxComponentDepth: " + Short.toUnsignedInt(maxComponentDepth) +
                ")";
    }
}
