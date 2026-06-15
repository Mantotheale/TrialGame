package com.game;

import com.game.collision.CollisionManager;
import com.game.collision.RectangleCollider;
import com.game.renderer.texture.Tile;
import com.game.resourcemanager.ResourceManager;
import com.game.transform.Scale2D;
import com.game.transform.Transform2D;
import com.game.transform.Translation2D;
import com.game.util.IOUtils;
import com.game.util.Vec2f;

import java.nio.file.Path;
import java.util.*;

public class TileMap implements Iterable<RenderComponent> {
    private final List<RenderComponent> tiles;

    public TileMap(List<RenderComponent> tiles) {
        this.tiles = new ArrayList<>(tiles);
    }

    @Override
    public Iterator<RenderComponent> iterator() {
        return Collections.unmodifiableList(tiles).iterator();
    }

    public static TileMap fromFile(Path path, ResourceManager resourceManager, CollisionManager collisionManager) {
        List<RenderComponent> tiles = IOUtils.readAllLines(path).stream()
                .filter(l -> !l.isBlank())
                .map(l -> l.split(" "))
                .map(fields -> {
                    System.out.println(Arrays.toString(fields));
                    float x = Float.parseFloat(fields[0]);
                    float y = Float.parseFloat(fields[1]);
                    float scaleX = Float.parseFloat(fields[2]);
                    float scaleY = Float.parseFloat(fields[3]);
                    int zIndex = Integer.parseInt(fields[4]);
                    Tile tile = Tile.valueOf(fields[5]);
                    boolean isSolid = Boolean.parseBoolean(fields[6]);

                    Transform2D transform = new Transform2D(
                            new Translation2D(x, y),
                            new Scale2D(scaleX, scaleY),
                            zIndex
                    );

                    if (isSolid) {
                        Entity map = new MapEntity(transform, resourceManager.getTexture(tile));
                        collisionManager.addCollider(map, new RectangleCollider(new Vec2f(x, y), scaleX, scaleY, false));
                    }

                    return new RenderComponent(transform, resourceManager.getTexture(tile));
                })
                .toList();

        return new TileMap(tiles);
    }
}
