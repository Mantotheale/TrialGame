package com.game.collision;

import com.game.entity.EntityId;
import com.game.event.bus.EventBus;
import com.game.event.deferred.CollisionEvent;
import com.game.event.deferred.EntityDeletedEvent;
import com.game.event.deferred.EntityMovedEvent;
import com.game.event.deferred.EntityVelocityChangedEvent;
import com.game.event.instant.CollisionsResolvedEvent;
import com.game.math.IntersectionData;
import com.game.math.Vec2f;

import java.util.*;

public class CollisionManager {
    private final Map<EntityId, ColliderState> colliders;

    public CollisionManager(EventBus bus) {
        colliders = new HashMap<>();

        bus.addObserver(EntityMovedEvent.class, this::onEntityMove);
        bus.addObserver(EntityVelocityChangedEvent.class, this::onEntityVelocityChanged);
        bus.addObserver(EntityDeletedEvent.class, this::onEntityDeleted);
    }

    public void addCollider(EntityId id, Collider collider) {
        colliders.put(id, new ColliderState(collider, Vec2f.ZERO));
    }

    public void removeCollider(EntityId id) {
        colliders.remove(id);
    }

    public void simulate(EventBus bus) {
        for (Map.Entry<EntityId, ColliderState> e: colliders.entrySet()) {
            EntityId id = e.getKey();
            ColliderState state = e.getValue();

            if (!state.collider.isFixed())
                simulateEntity(id, state, bus);
        }
    }

    private void simulateEntity(EntityId id, ColliderState state, EventBus bus) {
        Collider collider = state.collider;
        Vec2f velocity = state.velocity;

        Set<EntityId> collidedEntities = new HashSet<>();

        Optional<CollisionData> optCollisionData = nextCollision(id, collider, velocity, collidedEntities);
        while (optCollisionData.isPresent()) {
            CollisionData collisionData = optCollisionData.get();
            float collisionTime = collisionData.intersectionData.t();
            Vec2f collisionPoint = collider.center().add(velocity.mul(collisionTime));
            bus.postEvent(new CollisionEvent(id, collisionData.collidingEntity, collisionPoint));

            velocity = collider.resolveCollision(velocity, collisionData.intersectionData);
            collidedEntities.add(collisionData.collidingEntity);

            optCollisionData = nextCollision(id, collider, velocity, collidedEntities);
        }

        bus.postEvent(new CollisionsResolvedEvent(id, velocity));
    }

    private Optional<CollisionData> nextCollision(EntityId id, Collider collider, Vec2f velocity, Set<EntityId> alreadyCollided) {
        return colliders.entrySet().stream()
                .filter(c -> !c.getKey().equals(id))
                .filter(c -> !alreadyCollided.contains(c.getKey()))
                .map(c -> new PossibleCollisionData(
                        collider,
                        velocity,
                        c.getKey(),
                        c.getValue().collider
                ))
                .filter(c -> c.intersectionData.isPresent())
                .map(PossibleCollisionData::toCollisionData)
                .sorted()
                .findFirst();
    }

    private void onEntityMove(EventBus bus, EntityMovedEvent event) {
        colliders.computeIfPresent(
                event.entityId(),
                (_, state) -> state.positionChanged(event.position())
        );
    }

    private void onEntityVelocityChanged(EventBus bus, EntityVelocityChangedEvent event) {
        colliders.computeIfPresent(
                event.entityId(),
                (_, state) -> state.velocityChanged(event.velocity())
        );
    }

    private void onEntityDeleted(EventBus bus, EntityDeletedEvent event) {
        removeCollider(event.entityId());
    }

    private record ColliderState(Collider collider, Vec2f velocity) {
        public ColliderState positionChanged(Vec2f position) {
            return new ColliderState(collider.moveTo(position), velocity);
        }

        public ColliderState velocityChanged(Vec2f velocity) {
            return new ColliderState(collider, velocity);
        }
    }

    private record PossibleCollisionData(EntityId collidingEntity, Optional<IntersectionData> intersectionData, float squaredDistance) {
        public PossibleCollisionData(Collider collider, Vec2f velocity, EntityId otherEntity, Collider otherCollider) {
            this(
                   otherEntity,
                   collider.dynamicIntersection(velocity, otherCollider),
                   collider.squaredDistance(otherCollider)
            );
        }

        public CollisionData toCollisionData() {
            return new CollisionData(collidingEntity, intersectionData.orElseThrow(), squaredDistance);
        }
    }

    private record CollisionData(EntityId collidingEntity, IntersectionData intersectionData, float squaredDistance) implements Comparable<CollisionData> {
        @Override
        public int compareTo(CollisionData o) {
            int timesCmp = this.intersectionData.compareTo(o.intersectionData);
            if (timesCmp != 0) return timesCmp;

            return Float.compare(this.squaredDistance, o.squaredDistance);
        }
    }
}