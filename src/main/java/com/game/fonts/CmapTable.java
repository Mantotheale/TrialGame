package com.game.fonts;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.ArrayList;
import java.util.List;

record CmapTable(short version, short numTables, List<EncodingRecord> encodingRecords) {
    public static CmapTable fromChannel(ByteChannel channel, ByteBuffer u16Buffer, ByteBuffer u32Buffer) {
        short version = FontUtils.extract16Bit(channel, u16Buffer);
        short numTables = FontUtils.extract16Bit(channel, u16Buffer);

        List<EncodingRecord> encodingRecords = new ArrayList<>();
        for (int i = 0; i < numTables; i++)
            encodingRecords.add(EncodingRecord.fromChannel(channel, u16Buffer, u32Buffer));

        if (encodingRecords.isEmpty())
            throw new IllegalStateException("The encoding records of the cmap table are empty");

        return new CmapTable(version, numTables, encodingRecords);
    }

    @Override
    public @NotNull String toString() {
        StringBuilder sb = new StringBuilder()
                .append("CmapTable(\n")
                .append("\tversion:").append(Short.toUnsignedInt(version)).append(",\n")
                .append("\tnumTables:").append(Short.toUnsignedInt(numTables)).append(",\n")
                .append("\tencodingRecords:\n");

        for (EncodingRecord r: encodingRecords)
            sb.append("\t\t").append(r).append(",\n");

        sb.deleteCharAt(sb.length() - 1).deleteCharAt(sb.length() - 1)
                .append("\n").append(")");

        return sb.toString();
    }
}
