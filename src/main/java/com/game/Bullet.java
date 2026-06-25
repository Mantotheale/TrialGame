package com.game;

import com.game.collision.Collider;
import com.game.collision.CollisionManager;
import com.game.collision.CollisionType;
import com.game.collision.PhysicsState;
import com.game.entity.Entity;
import com.game.entity.EntityId;
import com.game.entity.EntityManager;
import com.game.event.bus.EventBus;
import com.game.event.bus.EventObserver;
import com.game.event.deferred.EntityDeletedEvent;
import com.game.event.deferred.EntityMovedEvent;
import com.game.event.deferred.EntityVelocityChangedEvent;
import com.game.event.instant.PhysicsUpdatedEvent;
import com.game.event.instant.RenderRequestEvent;
import com.game.event.instant.UpdateEvent;
import com.game.math.Circle;
import com.game.math.Vec2f;
import com.game.renderer.texture.Texture;
import com.game.renderer.texture.Tile;
import com.game.resourcemanager.ResourceManager;
import com.game.transform.Transform2D;
import com.game.transform.Translation2D;

import static com.game.Game.UPDATES_PER_SECOND;
import static com.game.Game.UPDATE_TIME;

public class Bullet implements Entity {
    private final EntityId id;
    private Transform2D transform;
    private final Texture texture;
    int elapsedUpdates;
    private Vec2f velocity;

    private final EventObserver<RenderRequestEvent> onRenderFunc = this::onRender;
    private final EventObserver<UpdateEvent> onUpdateFunc = this::onUpdate;
    private final EventObserver<PhysicsUpdatedEvent> onPhysicsUpdateFunc = this::onPhysicsUpdate;


    public Bullet(Transform2D transform, ResourceManager resourceManager, EntityManager entityManager, EventBus bus, CollisionManager collisionManager) {
        this.transform = transform;
        this.texture = resourceManager.getTexture(Tile.BULLET);
        elapsedUpdates = 0;
        id = entityManager.registerEntity(this);
        velocity = Vec2f.LEFT.mul(10f * (float) UPDATE_TIME);

        bus.addObserver(RenderRequestEvent.class, onRenderFunc);
        bus.addObserver(UpdateEvent.class, onUpdateFunc);
        bus.addObserver(PhysicsUpdatedEvent.class, onPhysicsUpdateFunc);

        collisionManager.addCollider(
                id,
                new Collider(
                        new Circle(
                                transform.translation().toVec2f(),
                                transform.scale().toVec2f().mul(0.8f).x() / 2
                        ),
                        CollisionType.ELASTIC,
                        false
                )
        );

        bus.postEvent(new EntityVelocityChangedEvent(id, velocity));
        bus.postEvent(new BulletCreated(id));
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
        if (elapsedUpdates == 100 * UPDATES_PER_SECOND)
            delete(bus);
    }

    private void onPhysicsUpdate(EventBus bus, PhysicsUpdatedEvent event) {
        PhysicsState state = event.collisionManager().state(id);
        this.velocity = state.velocity();
        this.transform = this.transform.translateTo(Translation2D.fromVec2f(state.position()));

        bus.postEvent(new EntityMovedEvent(id, transform.translation().toVec2f()));
        bus.postEvent(new EntityVelocityChangedEvent(id, velocity));
    }

    @Override
    public void delete(EventBus bus) {
        bus.removeObserver(RenderRequestEvent.class, onRenderFunc);
        bus.removeObserver(UpdateEvent.class, onUpdateFunc);
        bus.removeObserver(PhysicsUpdatedEvent.class, onPhysicsUpdateFunc);
        bus.postEvent(new EntityDeletedEvent(id));
    }
}
