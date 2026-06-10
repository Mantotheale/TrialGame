package com.game.renderer.texture.atlas;

import java.awt.image.BufferedImage;
import java.util.List;

public record ImageAndMetadata(BufferedImage image, List<TileMetadata> tileMetadata) { }
