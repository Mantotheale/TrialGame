package com.game.fonts;

import com.game.math.Rectangle;

import java.util.List;

public record GlyphData(
        int id,
        Rectangle boundingBox,
        List<FontPoint> points,
        List<Contour> contours,
        int advanceWidth,
        int leftSideBearing
) { }
