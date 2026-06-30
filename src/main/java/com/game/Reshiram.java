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
import com.game.event.deferred.CollisionEvent;
import com.game.event.deferred.EntityDeletedEvent;
import com.game.event.deferred.EntityMovedEvent;
import com.game.event.deferred.EntityVelocityChangedEvent;
import com.game.event.instant.*;
import com.game.input.InputManager;
import com.game.input.rawcomponents.KeyState;
import com.game.input.rawcomponents.PhysicalKey;
import com.game.math.Rectangle;
import com.game.math.Vec2f;
import com.game.renderer.texture.Texture;
import com.game.renderer.texture.Tile;
import com.game.resourcemanager.ResourceManager;
import com.game.transform.Transform2D;
import com.game.transform.Translation2D;

public class Reshiram implements Entity {
    private final EntityId id;
    private Transform2D transform;
    private final Texture texture;
    private Vec2f velocity;

    private final EventObserver<UpdateEvent> onUpdateFunc = this::onUpdate;
    private final EventObserver<PhysicsUpdatedEvent> onPhysicsUpdated = this::onPhysicsUpdated;
    private final EventObserver<RenderRequestEvent> onRenderFunc = this::onRender;
    private final EventObserver<CollisionEvent> onCollisionFunc = this::onCollision;

    @Override
    public EntityId id() {
        return id;
    }

    public Reshiram(Transform2D transform, ResourceManager resourceManager, EventBus bus, EntityManager entityManager, CollisionManager collisionManager) {
        this.transform = transform;
        this.texture = resourceManager.getTexture(Tile.RESHIRAM);
        this.velocity = Vec2f.ZERO;

        bus.addObserver(UpdateEvent.class, onUpdateFunc);
        bus.addObserver(PhysicsUpdatedEvent.class, onPhysicsUpdated);
        bus.addObserver(RenderRequestEvent.class, onRenderFunc);
        bus.addObserver(CollisionEvent.class, onCollisionFunc);

        this.id = entityManager.registerEntity(this);
        collisionManager.addCollider(
                id,
                new Collider(
                        /*new Circle(
                                transform.translation().toVec2f(),
                                transform.scale().toVec2f().mul(0.8f).x() / 2
                        )*/
                        new Rectangle(
                                transform.translation().toVec2f(),
                                transform.scale().toVec2f().scale(0.8f)
                        ),
                        CollisionType.INELASTIC,
                        true
                )
        );
    }

    private void onUpdate(EventBus bus, UpdateEvent event) {
        velocity = Vec2f.ZERO;

        InputManager inputManager = event.inputManager();
        Vec2f direction = Vec2f.ZERO;
        if (inputManager.keyState(PhysicalKey.W) == KeyState.DOWN)
            direction = direction.add(Vec2f.UP);
        if (inputManager.keyState(PhysicalKey.S) == KeyState.DOWN)
            direction = direction.add(Vec2f.DOWN);
        if (inputManager.keyState(PhysicalKey.A) == KeyState.DOWN)
            direction = direction.add(Vec2f.LEFT);
        if (inputManager.keyState(PhysicalKey.D) == KeyState.DOWN)
            direction = direction.add(Vec2f.RIGHT);

        if (!direction.equals(Vec2f.ZERO)) {
            float movementSpeed = 15 * (float) Game.UPDATE_TIME;
            velocity = direction.normalize().scale(movementSpeed);
        }

        bus.postEvent(new EntityVelocityChangedEvent(id, velocity));
    }

    private void onPhysicsUpdated(EventBus bus, PhysicsUpdatedEvent event) {
        PhysicsState state = event.collisionManager().state(id);
        this.velocity = state.velocity();
        this.transform = this.transform.translateTo(Translation2D.fromVec2f(state.position()));

        bus.postEvent(new EntityMovedEvent(id, state.position()));
        bus.postEvent(new EntityVelocityChangedEvent(id, velocity));
    }

    private void onRender(EventBus bus, RenderRequestEvent event) {
        event.renderer().submit(transform, texture);
    }

    private void onCollision(EventBus bus, CollisionEvent event) {
        if (event.e1().equals(id) || event.e2().equals(id)) {
            EntityId otherId = event.e1().equals(id) ? event.e2() : event.e1();
            Entity other = event.entityManager().getById(otherId);
            if (other instanceof Bullet) System.out.println("HIT");
        }
    }

    @Override
    public void delete(EventBus bus) {
        bus.removeObserver(UpdateEvent.class, onUpdateFunc);
        bus.removeObserver(PhysicsUpdatedEvent.class, onPhysicsUpdated);
        bus.removeObserver(RenderRequestEvent.class, onRenderFunc);
        bus.removeObserver(CollisionEvent.class, onCollisionFunc);

        bus.postEvent(new EntityDeletedEvent(id));
    }
}
