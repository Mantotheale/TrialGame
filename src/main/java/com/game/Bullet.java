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
import com.game.event.deferred.*;
import com.game.event.instant.PhysicsUpdatedEvent;
import com.game.event.instant.RenderRequestEvent;
import com.game.event.instant.UpdateEvent;
import com.game.math.Circle;
import com.game.math.Vec2f;
import com.game.renderer.texture.Texture;
import com.game.renderer.texture.Tile;
import com.game.resourcemanager.ResourceManager;
import com.game.sound.Sound;
import com.game.transform.Transform2D;
import com.game.transform.Translation2D;

import static com.game.Game.UPDATES_PER_SECOND;

public class Bullet implements Entity {
    private final EntityId id;
    private Transform2D transform;
    private final Texture texture;
    int elapsedUpdates;
    private Vec2f velocity;

    private final EventObserver<RenderRequestEvent> onRenderFunc = this::onRender;
    private final EventObserver<UpdateEvent> onUpdateFunc = this::onUpdate;
    private final EventObserver<PhysicsUpdatedEvent> onPhysicsUpdateFunc = this::onPhysicsUpdate;
    private final EventObserver<CollisionEvent> onCollisionFunc = this::onCollision;

    public Bullet(Transform2D transform, Vec2f startingVelocity, ResourceManager resourceManager, EntityManager entityManager, EventBus bus, CollisionManager collisionManager) {
        this.transform = transform;
        this.texture = resourceManager.getTexture(Tile.BULLET);
        elapsedUpdates = 0;
        id = entityManager.registerEntity(this);
        velocity = startingVelocity;

        bus.addObserver(RenderRequestEvent.class, onRenderFunc);
        bus.addObserver(UpdateEvent.class, onUpdateFunc);
        bus.addObserver(PhysicsUpdatedEvent.class, onPhysicsUpdateFunc);
        bus.addObserver(CollisionEvent.class, onCollisionFunc);

        collisionManager.addCollider(
                id,
                new Collider(
                        new Circle(
                                transform.translation().toVec2f(),
                                transform.scale().toVec2f().scale(0.8f).x() / 2
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
        if (elapsedUpdates == 50 * UPDATES_PER_SECOND)
            delete(bus);
    }

    private void onPhysicsUpdate(EventBus bus, PhysicsUpdatedEvent event) {
        PhysicsState state = event.collisionManager().state(id);
        this.velocity = state.velocity();
        this.transform = this.transform.translateTo(Translation2D.fromVec2f(state.position()));

        bus.postEvent(new EntityMovedEvent(id, transform.translation().toVec2f()));
        bus.postEvent(new EntityVelocityChangedEvent(id, velocity));
    }

    private void onCollision(EventBus bus, CollisionEvent event) {
        if (id.equals(event.e1()) || id.equals(event.e2())) {
            EntityId otherId = id.equals(event.e1()) ? event.e2() : event.e1();
            Entity other = event.entityManager().getById(otherId);
            if (other instanceof Reshiram) {
                bus.postEvent(PlaySoundRequestEvent.generateEvent(Sound.HIT, false, 0.7f));
                delete(bus);
            }
        }
    }

    @Override
    public void delete(EventBus bus) {
        bus.removeObserver(RenderRequestEvent.class, onRenderFunc);
        bus.removeObserver(UpdateEvent.class, onUpdateFunc);
        bus.removeObserver(PhysicsUpdatedEvent.class, onPhysicsUpdateFunc);
        bus.removeObserver(CollisionEvent.class, onCollisionFunc);

        bus.postEvent(new EntityDeletedEvent(id));
    }
}
