package com.game;

import com.game.entity.Entity;
import com.game.entity.EntityId;
import com.game.entity.EntityManager;
import com.game.event.bus.EventBus;
import com.game.event.bus.EventObserver;
import com.game.event.deferred.EntityDeletedEvent;
import com.game.event.deferred.EntityMovedEvent;
import com.game.event.deferred.EntityVelocityChangedEvent;
import com.game.event.instant.CanMoveEvent;
import com.game.event.instant.CollisionResolvedEvent;
import com.game.event.instant.RenderRequestEvent;
import com.game.event.instant.UpdateEvent;
import com.game.input.InputManager;
import com.game.input.rawcomponents.KeyState;
import com.game.input.rawcomponents.PhysicalKey;
import com.game.math.Vec2f;
import com.game.renderer.texture.Texture;
import com.game.renderer.texture.Tile;
import com.game.resourcemanager.ResourceManager;
import com.game.transform.Transform2D;

public class Reshiram implements Entity {
    private final EntityId id;
    private Transform2D transform;
    private final Texture texture;
    private final InputManager inputManager;
    private Vec2f velocity;

    private final EventObserver<UpdateEvent> onUpdateFunc = this::onUpdate;
    private final EventObserver<CollisionResolvedEvent> onCollisionResolvedFunc = this::onCollisionResolved;
    private final EventObserver<CanMoveEvent> onCanMoveFunc = this::onCanMove;
    private final EventObserver<RenderRequestEvent> onRenderFunc = this::onRender;

    @Override
    public EntityId id() {
        return id;
    }

    public Reshiram(Transform2D transform, InputManager inputManager, ResourceManager resourceManager, EventBus bus, EntityManager entityManager) {
        this.transform = transform;
        this.texture = resourceManager.getTexture(Tile.RESHIRAM);
        this.inputManager = inputManager;
        this.velocity = Vec2f.ZERO;

        bus.addObserver(UpdateEvent.class, onUpdateFunc);
        bus.addObserver(CollisionResolvedEvent.class, onCollisionResolvedFunc);
        bus.addObserver(CanMoveEvent.class, onCanMoveFunc);
        bus.addObserver(RenderRequestEvent.class, onRenderFunc);

        // TODO: TREAT THE COLLISION JUST BY ADDING A FUNCTION "RESOLVE COLLISION" ON THE COLLIDER, WHICH GIVEN THE COLLISION DATA, CALCULATES THE APPROPRIATE NEW VELOCITY AND RETURNS IT WITHOUT ACTUALLY CHANGING IT IN THE ENTITY. BY DOING THIS, THE COLLIDER CAN STORE THE COLLISION LOGIC WITHOUT THE ENTITY BEING RESPOSNIBLE FOR IT. THE ENTITY WILL MOVE LATER WITH THE COLLISION_RESOLVED EVENT

        this.id = entityManager.registerEntity(this);
    }

    private void onUpdate(EventBus bus, UpdateEvent updateEvent) {
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
            float movementSpeed = 20 * (float) Game.UPDATE_TIME;
            Vec2f velocity = direction.normalize().mul(movementSpeed);
            bus.postEvent(new EntityVelocityChangedEvent(id, velocity));
        }
    }

    private void onCollisionResolved(EventBus bus, CollisionResolvedEvent event) {
        if (event.entityId().equals(id)) {
            this.velocity = event.finalVelocity();
            bus.postEvent(new EntityVelocityChangedEvent(id, transform.translation().toVec2f()));
        }
    }

    private void onCanMove(EventBus bus, CanMoveEvent event) {
        transform = transform.translate(velocity);
        bus.postEvent(new EntityMovedEvent(id, transform.translation().toVec2f()));
    }

    private void onRender(EventBus bus, RenderRequestEvent event) {
        event.renderer().submit(transform, texture);
    }

    @Override
    public void delete(EventBus bus) {
        bus.removeObserver(UpdateEvent.class, onUpdateFunc);
        bus.removeObserver(CollisionResolvedEvent.class, onCollisionResolvedFunc);
        bus.removeObserver(CanMoveEvent.class, onCanMoveFunc);
        bus.removeObserver(RenderRequestEvent.class, onRenderFunc);

        bus.postEvent(new EntityDeletedEvent(id));
    }
}
