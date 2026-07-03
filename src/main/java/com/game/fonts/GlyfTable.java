package com.game.fonts;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

record GlyfTable(List<Glyph> glyphs) {
    public static GlyfTable fromChannel(FileChannel channel, ByteBuffer u8Buffer, ByteBuffer u16Buffer, int tableOffset, LocaTable locaTable) {
        try {
            List<Glyph> glyphs = new ArrayList<>();

            for (int i = 0; i < locaTable.offsetsAndLenghts().size(); i++) {
                LocaTable.OffsetAndLength ol = locaTable.offsetsAndLenghts().get(i);

                if (ol.length() == 0) {
                    glyphs.add(Glyph.EMPTY);
                } else {
                    channel.position(Integer.toUnsignedLong(tableOffset) + Integer.toUnsignedLong(ol.offset()));
                    glyphs.add(Glyph.fromChannel(channel, u16Buffer));
                }
            }

            return new GlyfTable(glyphs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull String toString() {
        StringBuilder sb = new StringBuilder()
                .append("GlyfTable(\n");

        for (Glyph glyph: glyphs)
            sb.append("\t").append(glyph).append(",\n");

        sb.deleteCharAt(sb.length() - 1).deleteCharAt(sb.length() - 1)
                .append("\n").append(")");

        return sb.toString();
    }
}
