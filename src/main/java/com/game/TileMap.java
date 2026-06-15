package com.game;

import com.game.renderer.texture.Tile;
import com.game.resourcemanager.ResourceManager;
import com.game.transform.Scale2D;
import com.game.transform.Transform2D;
import com.game.transform.Translation2D;
import com.game.util.IOUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class TileMap implements Iterable<RenderComponent> {
    private final List<RenderComponent> tiles;

    public TileMap(List<RenderComponent> tiles) {
        this.tiles = new ArrayList<>(tiles);
    }

    @Override
    public Iterator<RenderComponent> iterator() {
        return Collections.unmodifiableList(tiles).iterator();
    }

    public static TileMap fromFile(Path path, ResourceManager resourceManager) {
        List<RenderComponent> tiles = IOUtils.readAllLines(path).stream()
                .filter(l -> !l.isBlank())
                .map(l -> l.split(" "))
                .map(fields -> {
                    float x = Float.parseFloat(fields[0]);
                    float y = Float.parseFloat(fields[1]);
                    float scaleX = Float.parseFloat(fields[2]);
                    float scaleY = Float.parseFloat(fields[3]);
                    int zIndex = Integer.parseInt(fields[4]);
                    Tile tile = Tile.valueOf(fields[5]);

                    Transform2D transform = new Transform2D(
                            new Translation2D(x, y),
                            new Scale2D(scaleX, scaleY),
                            zIndex
                    );
                    return new RenderComponent(transform, resourceManager.getTexture(tile));
                })
                .toList();

        return new TileMap(tiles);
    }
}
