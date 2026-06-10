package com.game.renderer.texture.atlas;

import java.awt.image.BufferedImage;
import java.util.List;

record ImageAndMetadata(BufferedImage image, List<TileMetadata> tileMetadata) { }
