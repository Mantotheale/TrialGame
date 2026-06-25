package com.game.collision;

import com.game.entity.EntityId;
import com.game.event.bus.EventBus;
import com.game.event.deferred.CollisionEvent;
import com.game.event.deferred.EntityDeletedEvent;
import com.game.event.deferred.EntityMovedEvent;
import com.game.event.deferred.EntityVelocityChangedEvent;
import com.game.math.FloatUtils;
import com.game.math.IntersectionData;
import com.game.math.Vec2f;
import org.jetbrains.annotations.NotNull;

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

    public boolean isContained(EntityId id) {
        return colliders.containsKey(id);
    }

    public PhysicsState state(EntityId id) {
        ColliderState s = colliders.get(id);
        if (s == null) throw new NoSuchElementException("A collider associated with id " + id + " doesn't exist in the collision manager");

        return new PhysicsState(s.collider.center(), s.velocity);
    }

    public void removeCollider(EntityId id) {
        colliders.remove(id);
    }

    public void simulate(EventBus bus) {
        float time = 0;

        Optional<Collision> optCollision = nextCollision(time);
        while (optCollision.isPresent() && FloatUtils.lt(time, 1)) {
            Collision collision = optCollision.get();

            EntityId e1 = collision.e1;
            EntityId e2 = collision.e2;
            ColliderState cs = colliders.get(e1);
            Collider collider = cs.collider;
            Vec2f v1 = cs.velocity;

            IntersectionData data = collision.intersectionData;
            float dt = data.t();
            Vec2f normal = data.normal();

            colliders.replaceAll((_, s) -> s.advance(dt));
            Vec2f resolvedVelocity = collider.resolveCollision(v1, normal);
            colliders.computeIfPresent(e1, (_, s) -> s.velocityChanged(resolvedVelocity));
            bus.postEvent(new CollisionEvent(e1, e2));

            time += dt;
            optCollision = nextCollision(time);
        }

        float remainingTime = 1 - time;
        colliders.replaceAll((_, s) -> s.advance(remainingTime));
    }


    private Optional<Collision> nextCollision(float time) {
        return colliders.entrySet().stream()
                .filter(e1 -> !e1.getValue().collider.isFixed())
                .flatMap(e1 -> colliders.entrySet().stream()
                        .filter(e2 -> !e1.getKey().equals(e2.getKey()))
                        .flatMap(e2 -> {
                            EntityId id1 = e1.getKey();
                            ColliderState cs1 = e1.getValue();
                            EntityId id2 = e2.getKey();
                            ColliderState cs2 = e2.getValue();

                            return cs1.collider.dynamicIntersection(cs1.velocity, cs2.collider)
                                    .filter(data -> FloatUtils.lt(cs1.velocity.dot(data.normal()), 0))
                                    .filter(data -> FloatUtils.leq(time + data.t(), 1))
                                    .map(data -> {
                                        float squaredDistance = cs1.collider().squaredDistance(cs2.collider());
                                        return new Collision(id1, id2, data, squaredDistance);
                                    })
                                    .stream();
                        })
                )
                .min(Comparator.naturalOrder());
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

    private record Collision(
            EntityId e1,
            EntityId e2,
            IntersectionData intersectionData,
            float squaredDistance
    ) implements Comparable<Collision> {
        @Override
        public int compareTo(@NotNull CollisionManager.Collision o) {
            int timeCmp = intersectionData.compareTo(o.intersectionData);
            if (timeCmp != 0) return timeCmp;

            return FloatUtils.EPS_COMPARATOR.compare(squaredDistance, o.squaredDistance);
        }
    }
}