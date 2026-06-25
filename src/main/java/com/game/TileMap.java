package com.game;

import com.game.collision.Collider;
import com.game.collision.CollisionManager;
import com.game.entity.Entity;
import com.game.entity.EntityManager;
import com.game.event.bus.EventBus;
import com.game.math.Rectangle;
import com.game.renderer.texture.Tile;
import com.game.resourcemanager.ResourceManager;
import com.game.transform.Scale2D;
import com.game.transform.Transform2D;
import com.game.transform.Translation2D;
import com.game.util.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;

public class TileMap implements Iterable<RenderComponent> {
    private final List<RenderComponent> tiles;

    public TileMap(List<RenderComponent> tiles) {
        this.tiles = new ArrayList<>(tiles);
    }

    @Override
    public @NotNull Iterator<RenderComponent> iterator() {
        return Collections.unmodifiableList(tiles).iterator();
    }

    public static TileMap fromFile(Path path, ResourceManager resourceManager, EventBus eventBus, EntityManager entityManager, CollisionManager collisionManager) {
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
                    boolean isSolid = Boolean.parseBoolean(fields[6]);

                    Transform2D transform = new Transform2D(
                            new Translation2D(x, y),
                            new Scale2D(scaleX, scaleY),
                            zIndex
                    );

                    if (isSolid) {
                        Entity entity = new MapElementEntity(transform, resourceManager.getTexture(tile), eventBus, entityManager);
                        collisionManager.addCollider(
                                entity.id(),
                                new Collider(
                                        /*new Circle(
                                                transform.translation().toVec2f(),
                                                transform.scale().toVec2f().x() / 2
                                        )*/
                                        new Rectangle(
                                                transform.translation().toVec2f(),
                                                transform.scale().toVec2f()
                                        ),
                                        true
                                )
                        );
                    }

                    return new RenderComponent(transform, resourceManager.getTexture(tile));
                })
                .toList();

        return new TileMap(tiles);
    }
}
