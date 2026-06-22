package com.game;

import com.game.entity.Entity;
import com.game.entity.EntityId;
import com.game.entity.EntityManager;
import com.game.event.bus.EventBus;
import com.game.event.deferred.EntityMovedEvent;
import com.game.event.instant.MoveRequestEvent;
import com.game.event.instant.RenderRequestEvent;
import com.game.event.instant.UpdateEvent;
import com.game.math.Vec2f;
import com.game.renderer.texture.Texture;
import com.game.renderer.texture.Tile;
import com.game.resourcemanager.ResourceManager;
import com.game.transform.Transform2D;

import static com.game.Game.UPDATES_PER_SECOND;
import static com.game.Game.UPDATE_TIME;

public class Bullet implements Entity {
    private final EntityId id;
    private Transform2D transform;
    private final Texture texture;
    int elapsedUpdates;
    private final Vec2f velocity;

    public Bullet(Transform2D transform, ResourceManager resourceManager, EntityManager entityManager, EventBus bus) {
        this.transform = transform;
        this.texture = resourceManager.getTexture(Tile.BULLET);
        elapsedUpdates = 0;
        id = entityManager.registerEntity(this);
        velocity = Vec2f.LEFT.mul((float) UPDATE_TIME);

        bus.addObserver(RenderRequestEvent.class, this::onRender);
        bus.addObserver(UpdateEvent.class, this::onUpdate);
        bus.addObserver(MoveRequestEvent.class, this::onMove);
    }

    @Override
    public EntityId id() {
        return id;
    }

    public void onRender(EventBus bus, RenderRequestEvent event) {
        event.renderer().submit(transform, texture);
    }

    public void onUpdate(EventBus bus, UpdateEvent event) {
        elapsedUpdates++;
        if (elapsedUpdates == 5 * UPDATES_PER_SECOND) {
            bus.removeObserver(UpdateEvent.class, this::onUpdate);
        }
    }

    public void onMove(EventBus bus, MoveRequestEvent event) {
        transform = transform.translate(velocity);
        bus.postEvent(new EntityMovedEvent(id, transform.translation().toVec2f()));
    }

    /*
    @Override
    public void onEvent(EventBus bus, InstantEvent event) {
        switch (event) {
            case RenderRequestEvent(Renderer renderer) -> renderer.submit(transform, texture);
            case UpdateEvent() -> {
                elapsedUpdates++;
                if (elapsedUpdates == 5 * UPDATES_PER_SECOND) {
                    bus.removeInstantObserver(this);
                } else {
                    transform = transform.translate(-1 * (float) UPDATE_TIME, 0);
                    bus.postDeferredEvent(new EntityMovedEvent(this, transform.translation().toVec2f()));
                }
            }
            default -> { }
        }
    }*/
}
