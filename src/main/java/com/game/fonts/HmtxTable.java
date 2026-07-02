package com.game.fonts;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.ArrayList;
import java.util.List;

record HmtxTable(List<LongHorMetric> horizontalMetrics) {
    public static HmtxTable fromChannel(ByteChannel channel, ByteBuffer u16Buffer, short numGlyphs, short numberOfMetrics) {
        List<Short> advanceWidths = new ArrayList<>();
        List<Short> leftSideBearings = new ArrayList<>();

        for (int i = 0; i < numberOfMetrics; i++) {
            advanceWidths.add(FontUtils.extract16Bit(channel, u16Buffer));
            leftSideBearings.add(FontUtils.extract16Bit(channel, u16Buffer));
        }

        for (int i = 0; i < numGlyphs - numberOfMetrics; i++) {
            advanceWidths.add(advanceWidths.getLast());
            leftSideBearings.add(FontUtils.extract16Bit(channel, u16Buffer));
        }

        List<LongHorMetric> horizontalMetrics = new ArrayList<>();
        for (int i = 0; i < numGlyphs; i++) {
            horizontalMetrics.add(new LongHorMetric(advanceWidths.get(i), leftSideBearings.get(i)));
        }

        return new HmtxTable(horizontalMetrics);
    }

    @Override
    public @NotNull String toString() {
        StringBuilder sb = new StringBuilder()
                .append("HtmxTable(\n");

        for (LongHorMetric hm: horizontalMetrics)
            sb.append("\t").append(hm).append(",\n");

        sb.deleteCharAt(sb.length() - 1).deleteCharAt(sb.length() - 1)
                .append("\n").append(")");

        return sb.toString();
    }
}
