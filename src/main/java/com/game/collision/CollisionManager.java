package com.game.collision;

import com.game.entity.EntityId;
import com.game.event.bus.EventBus;
import com.game.event.deferred.CollisionEvent;
import com.game.event.deferred.EntityDeletedEvent;
import com.game.event.deferred.EntityMovedEvent;
import com.game.event.deferred.EntityVelocityChangedEvent;
import com.game.event.instant.CollisionsResolvedEvent;
import com.game.math.FloatUtils;
import com.game.math.IntersectionData;
import com.game.math.IntersectionUtils;
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
        System.out.println("START");
        float previousTime = 0;
        Set<EntityPair> collidedPairs = new HashSet<>();
        Optional<Collision> optCollision = nextCollision(collidedPairs);
        while (optCollision.isPresent() && FloatUtils.lt(previousTime, 1)) {
            Collision collision = optCollision.get();
            float dt = collision.intersectionData.t();

            float actualDt = FloatUtils.gt(previousTime + dt, 1) ? 1 - previousTime : dt;
            colliders.replaceAll((_, s) -> s.advance(actualDt));

            EntityId e1 = collision.e1;
            ColliderState state1 = colliders.get(e1);
            System.out.println("Starting velocity: " + colliders.get(e1).velocity);
            Vec2f resolvedVelocity = IntersectionUtils.resolveDynamicIntersection(state1.velocity, collision.intersectionData);
            colliders.compute(e1, (_, s) -> s.velocityChanged(resolvedVelocity));
            System.out.println("Resulting velocity: " + resolvedVelocity);
            collidedPairs.add(new EntityPair(collision.e1, collision.e2));
            previousTime += dt;
            optCollision = nextCollision(collidedPairs);
        }

        float lastDt = FloatUtils.lt(previousTime, 1) ? 1 - previousTime : 0;
        colliders.replaceAll((_, s) -> s.advance(lastDt));

        for (Map.Entry<EntityId, ColliderState> e: colliders.entrySet()) {
            EntityId id = e.getKey();
            ColliderState state = e.getValue();
            bus.postEvent(new CollisionsResolvedEvent(id, state.velocity, state.collider.center()));
        }

        System.out.println("END");
    }

    private record EntityPair(EntityId e1, EntityId e2) { }

    private record Collision(EntityId e1, EntityId e2, IntersectionData intersectionData, float squaredDistance) { }

    private Optional<Collision> nextCollision(Set<EntityPair> alreadyCollided) {
        Collision nextCollision = null;

        for (Map.Entry<EntityId, ColliderState> e1: colliders.entrySet()) {
            EntityId id1 = e1.getKey();
            ColliderState state1 = e1.getValue();
            Collider collider1 = state1.collider;
            Vec2f velocity1 = state1.velocity;

            if (!state1.collider.isFixed()) {
                for (Map.Entry<EntityId, ColliderState> e2: colliders.entrySet()) {
                    EntityId id2 = e2.getKey();
                    if (id1.equals(id2)) continue;
                    if (alreadyCollided.contains(new EntityPair(id1, id2))) continue;

                    ColliderState state2 = e2.getValue();
                    Collider collider2 = state2.collider;

                    float squaredDistance = state1.collider.squaredDistance(state2.collider);
                    Optional<IntersectionData> optIntersectionData = collider1.dynamicIntersection(velocity1, collider2);
                    if (optIntersectionData.isPresent()) {
                        IntersectionData data = optIntersectionData.get();
                        if (nextCollision == null
                                || FloatUtils.lt(data.t(), nextCollision.intersectionData.t())
                                || (FloatUtils.eq(data.t(), nextCollision.intersectionData.t()) && FloatUtils.eq(squaredDistance, nextCollision.squaredDistance))) {
                            nextCollision = new Collision(id1, id2, data, squaredDistance);
                        }
                    }
                }
            }
        }

        System.out.println(nextCollision);
        return Optional.ofNullable(nextCollision);
    }

    private void simulateEntity(EntityId id, ColliderState state, EventBus bus) {
        Collider collider = state.collider;
        Vec2f velocity = state.velocity;

        Set<EntityId> collidedEntities = new HashSet<>();

        System.out.println("---------------");
        System.out.println("Starting position: " + state.collider.center());
        System.out.println("Starting velocity: " + state.velocity);
        System.out.println("Collider data: " + state.collider);
        Optional<CollisionData> optCollisionData = nextCollision(id, collider, velocity, collidedEntities);
        while (optCollisionData.isPresent()) {
            CollisionData collisionData = optCollisionData.get();
            System.out.println("Colliding entity: " + collisionData.collidingEntity);
            System.out.println("Entity collider: " + colliders.get(collisionData.collidingEntity));
            System.out.println("Collision time: " + collisionData.intersectionData.t());
            System.out.println("Collision normal: " + collisionData.intersectionData.normal());
            float collisionTime = collisionData.intersectionData.t();
            Vec2f collisionPoint = collider.center().add(velocity.mul(collisionTime));
            bus.postEvent(new CollisionEvent(id, collisionData.collidingEntity, collisionPoint));

            velocity = collider.resolveCollision(velocity, collisionData.intersectionData);
            System.out.println("Resolved velocity: " + velocity);
            collidedEntities.add(collisionData.collidingEntity);

            optCollisionData = nextCollision(id, collider, velocity, collidedEntities);
        }
        System.out.println("---------------");

        //bus.postEvent(new CollisionsResolvedEvent(id, velocity));
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

        public ColliderState advance(float dt) {
            return positionChanged(collider.center().add(velocity.mul(dt)));
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