package com.game;

import com.game.collision.Collider;
import com.game.collision.CollisionManager;
import com.game.collision.CollisionType;
import com.game.entity.Entity;
import com.game.entity.EntityId;
import com.game.entity.EntityManager;
import com.game.event.bus.EventBus;
import com.game.event.bus.EventObserver;
import com.game.event.deferred.EntityDeletedEvent;
import com.game.event.instant.RenderRequestEvent;
import com.game.event.instant.UpdateEvent;
import com.game.math.Circle;
import com.game.math.Vec2f;
import com.game.renderer.texture.Texture;
import com.game.renderer.texture.Tile;
import com.game.resourcemanager.ResourceManager;
import com.game.transform.Scale2D;
import com.game.transform.Transform2D;

import java.util.function.Supplier;

import static com.game.Game.UPDATE_TIME;

public class MewTwo implements Entity {
    private final EntityId id;
    private final Transform2D transform;
    private final Texture texture;
    private int frames;
    private Supplier<Vec2f> targetPositionSupplier;

    private final EventObserver<UpdateEvent> onUpdateFunc = this::onUpdate;
    private final EventObserver<RenderRequestEvent> onRenderFunc = this::onRender;

    public MewTwo(Transform2D transform, ResourceManager resourceManager, EventBus bus, EntityManager entityManager, CollisionManager collisionManager) {
        this.transform = transform;
        this.texture = resourceManager.getTexture(Tile.MEWTWO);
        frames = 0;

        bus.addObserver(UpdateEvent.class, onUpdateFunc);
        bus.addObserver(RenderRequestEvent.class, onRenderFunc);

        this.id = entityManager.registerEntity(this);

        collisionManager.addCollider(
                id,
                new Collider(
                        new Circle(
                                transform.translation().toVec2f(),
                                transform.scale().toVec2f().mul(0.8f).x() / 2
                        )/*
                        new Rectangle(
                                transform.translation().toVec2f(),
                                transform.scale().toVec2f().mul(0.8f)
                        )*/,
                        CollisionType.INELASTIC,
                        true
                )
        );

        targetPositionSupplier = null;
    }

    @Override
    public EntityId id() {
        return id;
    }

    public void setTargetPosition(Supplier<Vec2f> supplier) {
        targetPositionSupplier = supplier;
    }

    private void onUpdate(EventBus bus, UpdateEvent event) {
        frames++;

        if (frames == 60) {
            Vec2f position = transform.translation().toVec2f();
            Vec2f target = targetPositionSupplier != null
                    ? targetPositionSupplier.get()
                    : position.add(Vec2f.LEFT);
            Vec2f direction = target.sub(position).normalize();
            float baseSpeed = 7.5f * (float) UPDATE_TIME;

            new Bullet(
                    new Transform2D(
                            transform.translation().compose(direction),
                            Scale2D.UNIT,
                            3
                    ),
                    direction.mul(baseSpeed),
                    event.resourceManager(),
                    event.entityManager(),
                    bus,
                    event.collisionManager()
            );
            frames = 0;
        }
    }

    private void onRender(EventBus bus, RenderRequestEvent event) {
        event.renderer().submit(transform, texture);
    }

    @Override
    public void delete(EventBus bus) {
        bus.removeObserver(UpdateEvent.class, onUpdateFunc);
        bus.removeObserver(RenderRequestEvent.class, onRenderFunc);
        bus.postEvent(new EntityDeletedEvent(id));
    }
}
