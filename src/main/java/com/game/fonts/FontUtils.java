package com.game.fonts;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ByteChannel;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

public class FontUtils {
    private FontUtils() { }

    public static FontData openFont(Path path) {
        try(FileInputStream s = new FileInputStream(path.toFile())) {
            FileChannel channel = s.getChannel();
            ByteBuffer u64Buffer = ByteBuffer.allocateDirect(8).order(ByteOrder.BIG_ENDIAN);
            ByteBuffer u32Buffer = ByteBuffer.allocateDirect(4).order(ByteOrder.BIG_ENDIAN);
            ByteBuffer u16Buffer = ByteBuffer.allocateDirect(2).order(ByteOrder.BIG_ENDIAN);
            ByteBuffer u8Buffer = ByteBuffer.allocateDirect(1).order(ByteOrder.BIG_ENDIAN);

            OffsetSubtable offsetSubtable = OffsetSubtable.fromChannel(channel, u16Buffer, u32Buffer, path);
            List<TableHeader> tableHeaders = new ArrayList<>();
            for (int i = 0; i < offsetSubtable.numTables(); i++)
                tableHeaders.add(TableHeader.fromChannel(channel, u32Buffer));

            TableHeader cmapTableHeader = tableHeaders.stream()
                    .filter(h -> h.tag().equals("cmap"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("The specified font doesn't contain a cmap table"));
            channel.position(cmapTableHeader.offset());

            CmapTable cmapTable = CmapTable.fromChannel(channel, u16Buffer, u32Buffer);
            EncodingRecord bestEncoding = cmapTable.encodingRecords().stream().max(EncodingRecord.QUALITY_COMPARATOR).orElseThrow();
            if (!bestEncoding.isUnicodeMapping())
                throw new IllegalStateException("No unicode mapping is supported");

            int bestEncodingOffset = cmapTableHeader.offset() + bestEncoding.offset();
            channel.position(bestEncodingOffset);

            short version = extract16Bit(channel, u16Buffer);
            Cmap cmap = Cmap.fromChannel(version, channel, u16Buffer, u32Buffer);

            TableHeader headTableHeader = tableHeaders.stream()
                    .filter(h -> h.tag().equals("head"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("The specified font doesn't contain a head table"));
            channel.position(headTableHeader.offset());

            HeadTable headTable = HeadTable.fromChannel(channel, u16Buffer, u32Buffer, u64Buffer);
            //System.out.println(headTable);

            TableHeader hheaTableHeader = tableHeaders.stream()
                    .filter(h -> h.tag().equals("hhea"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("The specified font doesn't contain a hhea table"));
            channel.position(hheaTableHeader.offset());

            HheaTable hheaTable = HheaTable.fromChannel(channel, u16Buffer);
            //System.out.println(hheaTable);

            TableHeader maxpTableHeader = tableHeaders.stream()
                    .filter(h -> h.tag().equals("maxp"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("The specified font doesn't contain a maxp table"));
            channel.position(maxpTableHeader.offset());

            short majorVersion = extract16Bit(channel, u16Buffer);
            short minorVersion = extract16Bit(channel, u16Buffer);
            if (majorVersion != 1)
                throw new IllegalStateException("Unsupported maxp table major version. Only version 1 is supported. It was " + Short.toUnsignedInt(majorVersion));
            if (Short.toUnsignedInt(minorVersion) != 0)
                throw new IllegalStateException("Unsupported maxp table minor version. Only version 0 is supported. It was " + Short.toUnsignedInt(minorVersion));

            MaxpTable maxpTable = MaxpTable.fromChannel(channel, u16Buffer);
            //System.out.println(maxpTable);

            TableHeader hmtxTableHeader = tableHeaders.stream()
                    .filter(h -> h.tag().equals("hmtx"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("The specified font doesn't contain a hmtx table"));
            channel.position(hmtxTableHeader.offset());

            HmtxTable hmtxTable = HmtxTable.fromChannel(channel, u16Buffer, maxpTable.numGlyphs(), hheaTable.numberOfHMetrics());
            //System.out.println(hmtxTable);

            TableHeader locaTableHeader = tableHeaders.stream()
                    .filter(h -> h.tag().equals("loca"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("The specified font doesn't contain a loca table"));
            channel.position(locaTableHeader.offset());

            LocaTable locaTable;
            if (headTable.indexToLocFormat() == 0) {
                locaTable = LocaTable.fromChannelIndexShort(channel, u16Buffer, maxpTable.numGlyphs());
            } else if (headTable.indexToLocFormat() == 1) {
                locaTable = LocaTable.fromChannelIndexInt(channel, u32Buffer, maxpTable.numGlyphs());
            } else
                throw new IllegalStateException("The specified indexToLocFormat is unsupported. Only 0 and 1 are supported. It was " + headTable.indexToLocFormat());

            TableHeader glyfTableHeader = tableHeaders.stream()
                    .filter(h -> h.tag().equals("glyf"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("The specified font doesn't contain a glyf table"));
            GlyfTable glyfTable = GlyfTable.fromChannel(channel, u8Buffer, u16Buffer, glyfTableHeader.offset(), locaTable);

            return new FontData(headTable, cmap, hmtxTable, glyfTable, hheaTable);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static int extract32Bit(ByteChannel channel, ByteBuffer u32Buffer) {
        try {
            int readBytes = channel.read(u32Buffer);
            if (readBytes != 4) throw new IllegalStateException("The buffer couldn't read 4 bytes. It read " + readBytes);
            u32Buffer.flip();
            int value = u32Buffer.getInt();
            u32Buffer.flip();
            return value;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static short extract16Bit(ByteChannel channel, ByteBuffer u16Buffer) {
        try {
            int readBytes = channel.read(u16Buffer);
            if (readBytes != 2) throw new IllegalStateException("The buffer couldn't read 2 bytes. It read " + readBytes);
            u16Buffer.flip();
            short value = u16Buffer.getShort();
            u16Buffer.flip();
            return value;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static long extract64Bit(ByteChannel channel, ByteBuffer u64Buffer) {
        try {
            int readBytes = channel.read(u64Buffer);
            if (readBytes != 8) throw new IllegalStateException("The buffer couldn't read 8 bytes. It read " + readBytes);
            u64Buffer.flip();
            long value = u64Buffer.getLong();
            u64Buffer.flip();
            return value;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte extract8Bit(ByteChannel channel, ByteBuffer u8Buffer) {
        try {
            int readBytes = channel.read(u8Buffer);
            if (readBytes != 1) throw new IllegalStateException("The buffer couldn't read 1 bytes. It read " + readBytes);
            u8Buffer.flip();
            byte value = u8Buffer.get();
            u8Buffer.flip();
            return value;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static float extractF2Dot14(ByteChannel channel, ByteBuffer u16Buffer) {
        try {
            int readBytes = channel.read(u16Buffer);
            if (readBytes != 2) throw new IllegalStateException("The buffer couldn't read 2 bytes. It read " + readBytes);
            u16Buffer.flip();
            short value = u16Buffer.getShort();
            u16Buffer.flip();
            return value / 16384.0f; // value / 2^14
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String toHex(ByteBuffer buffer) {
        StringBuilder sb = new StringBuilder();
        sb.append("0x");

        ByteBuffer readOnly = buffer.duplicate();
        while (readOnly.hasRemaining())
            sb.append(HexFormat.of().toHexDigits(readOnly.get()));

        return sb.toString();
    }
}
