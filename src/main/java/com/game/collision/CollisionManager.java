package com.game.collision;

import com.game.Entity;
import com.game.event.*;
import com.game.util.Vec2f;

import java.util.HashMap;
import java.util.Map;

public class CollisionManager implements EventObserver {
    private final Map<Entity, Collider> colliders;

    public CollisionManager() {
        colliders = new HashMap<>();
    }

    public void addCollider(Entity entity, Collider collider) {
        colliders.put(entity, collider);
    }

    public void removeCollider(Entity entity) {
        colliders.remove(entity);
    }

    public void findCollisions(EventDispatcher dispatcher) {
        colliders.entrySet().stream()
                .filter(e -> e.getValue().isMobile())
                .forEach(e1 -> colliders.entrySet().stream()
                        .filter(e2 -> !e2.getKey().equals(e1.getKey()))
                        .forEach(e2 -> handlePotentialCollision(dispatcher, e1, e2))
                );
    }

    private void handlePotentialCollision(EventDispatcher dispatcher, Map.Entry<Entity, Collider> e1, Map.Entry<Entity, Collider> e2) {
        e1.getValue().minimumTranslationVector(e2.getValue())
                .ifPresent(mtv -> dispatcher.pushEvent(new CollisionEvent(e1.getKey(), e2.getKey(), mtv)));
    }

    @Override
    public void onEvent(EventDispatcher dispatcher, Event event) {
        if (event instanceof EntityMovedEvent(Entity entity, Vec2f position))
            colliders.computeIfPresent(entity, (_, collider) -> collider.moveToPosition(position));
    }
}
