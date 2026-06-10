package com.game.renderer.texture.atlas;

import java.nio.file.Path;
import java.util.List;

public record PathAndMetadata(Path path, List<TileMetadata> tilesMetadata) { }
