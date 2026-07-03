package com.game.fonts;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

sealed interface Glyph {
    GlyphHeader glyphHeader();

    Glyph EMPTY = new EmptyGlyph();

    record EmptyGlyph() implements Glyph {
        @Override
        public GlyphHeader glyphHeader() { return null; }

        @Override
        public @NotNull String toString() {
            return "EmptyGlyph";
        }
    }

    record SimpleGlyph(GlyphHeader glyphHeader) implements Glyph {
        @Override
        public @NotNull String toString() {
            return "SimpleGlyph(\n" +
                    "\tglyphHeader: " + glyphHeader + "\n" +
                    ")";
        }
    }
    record CompositeGlyph(GlyphHeader glyphHeader) implements Glyph {
        @Override
        public @NotNull String toString() {
            return "CompositeGlyph(\n" +
                    "\tglyphHeader: " + glyphHeader + "\n" +
                    ")";
        }
    }

    static Glyph fromChannel(ByteChannel channel, ByteBuffer u16Buffer) {
        GlyphHeader glyphHeader = GlyphHeader.fromChannel(channel, u16Buffer);

        if (glyphHeader.numberOfCountours() >= 0)
            return new SimpleGlyph(glyphHeader);
        else
            return new CompositeGlyph(glyphHeader);
    }
}
