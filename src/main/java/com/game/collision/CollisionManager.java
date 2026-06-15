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
        for (Map.Entry<Entity, Collider> entry1: colliders.entrySet()) {
            Entity entity1 = entry1.getKey();
            Collider collider1 = entry1.getValue();

            colliders.entrySet().stream()
                    .filter(entry2 -> !entry2.getKey().equals(entity1))
                    .filter(entry2 -> areColliding(collider1, entry2.getValue()))
                    .forEach(entry2 ->
                            dispatcher.pushEvent(
                                new CollisionEvent(entity1, entry2.getKey(), collider1, entry2.getValue())
                            )
                    );
        }
    }

    @Override
    public void onEvent(EventDispatcher dispatcher, Event event) {
        if (event instanceof EntityMovedEvent(Entity entity, Vec2f position)) {
            System.out.println("ENTITY MOVED" + event);
            colliders.computeIfPresent(
                    entity,
                    (_, collider) ->
                            new Collider(position, collider.width(), collider.height())
            );
        }
    }

    private static boolean areColliding(Collider collider1, Collider collider2) {
        Vec2f c1 = collider1.center();
        float w1 = collider1.width() / 2;
        float h1 = collider1.height() / 2;

        Vec2f c2 = collider2.center();
        float w2 = collider2.width() / 2;
        float h2 = collider2.height() / 2;

        if (c2.x() + w2 < c1.x() - w1) return false;
        if (c2.x() - w2 > c1.x() + w1) return false;
        if (c2.y() - h2 > c1.y() + h1) return false;
        return !(c2.y() + h2 < c1.y() - h1);
    }
}
