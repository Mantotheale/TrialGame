package com.game.fonts;

import com.game.math.Rectangle;
import com.game.math.Vec2f;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

record GlyphHeader(
        short idx,
        short numberOfContours,
        short xMin,
        short yMin,
        short xMax,
        short yMax
) {
    public static GlyphHeader fromChannel(ByteChannel channel, ByteBuffer u16Buffer, short idx) {
        return new GlyphHeader(
                idx,
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract16Bit(channel, u16Buffer)
        );
    }

    public Rectangle boundingBox() {
        Vec2f bboxCenter = new Vec2f(
                xMin + (xMax - xMin) / 2f,
                yMin + (yMax - yMin) / 2f
        );
        Vec2f bboxDimensions = new Vec2f(xMax - xMin, yMax - yMin);
        return new Rectangle(bboxCenter, bboxDimensions);
    }

    @Override
    public @NotNull String toString() {
        return "GlyphHeader(" +
                "idx: " + Short.toUnsignedInt(idx) + ", " +
                "numberOfContours: " + numberOfContours + ", " +
                "xMin: " + xMin + ", " +
                "yMin: " + yMin + ", " +
                "xMax: " + xMax + ", " +
                "yMax: " + yMax +
                ")";
    }
}
