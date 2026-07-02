package com.game.fonts;

import org.jetbrains.annotations.NotNull;

record UnicodeSegment(short startCode, short endCode, short idDelta, short idRangeOffset) {
    @Override
    public @NotNull String toString() {
        return "UnicodeSegment(" +
                "startCode: " + Short.toUnsignedInt(startCode) + ", " +
                "endCode: " + Short.toUnsignedInt(endCode) + ", " +
                "idDelta: " + idDelta + ", " +
                "idRangeOffset: " + Short.toUnsignedInt(idRangeOffset) +
                ")";
    }
}
