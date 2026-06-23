package com.game;

import com.game.entity.Entity;
import com.game.entity.EntityId;
import com.game.entity.EntityManager;
import com.game.event.bus.EventBus;
import com.game.event.bus.EventObserver;
import com.game.event.deferred.EntityDeletedEvent;
import com.game.event.deferred.EntityMovedEvent;
import com.game.event.instant.CanMoveEvent;
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

    private final EventObserver<RenderRequestEvent> onRenderFunc = this::onRender;
    private final EventObserver<UpdateEvent> onUpdateFunc = this::onUpdate;
    private final EventObserver<CanMoveEvent> onMoveFunc = this::onMove;


    public Bullet(Transform2D transform, ResourceManager resourceManager, EntityManager entityManager, EventBus bus) {
        this.transform = transform;
        this.texture = resourceManager.getTexture(Tile.BULLET);
        elapsedUpdates = 0;
        id = entityManager.registerEntity(this);
        velocity = Vec2f.LEFT.mul((float) UPDATE_TIME);

        bus.addObserver(RenderRequestEvent.class, onRenderFunc);
        bus.addObserver(UpdateEvent.class, onUpdateFunc);
        bus.addObserver(CanMoveEvent.class, onMoveFunc);
    }

    @Override
    public EntityId id() {
        return id;
    }

    private void onRender(EventBus bus, RenderRequestEvent event) {
        event.renderer().submit(transform, texture);
    }

    private void onUpdate(EventBus bus, UpdateEvent event) {
        elapsedUpdates++;
        if (elapsedUpdates == 5 * UPDATES_PER_SECOND)
            delete(bus);
    }

    private void onMove(EventBus bus, CanMoveEvent event) {
        transform = transform.translate(velocity);
        bus.postEvent(new EntityMovedEvent(id, transform.translation().toVec2f()));
    }

    @Override
    public void delete(EventBus bus) {
        bus.removeObserver(RenderRequestEvent.class, onRenderFunc);
        bus.removeObserver(UpdateEvent.class, onUpdateFunc);
        bus.removeObserver(CanMoveEvent.class, onMoveFunc);
        bus.postEvent(new EntityDeletedEvent(id));
    }
}
