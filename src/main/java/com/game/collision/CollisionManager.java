package com.game.collision;

import com.game.Entity;
import com.game.event.*;
import com.game.event.collision.CollisionEvent;
import com.game.util.Vec2f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CollisionManager implements EventObserver {
    private final Map<Entity, ColliderEvolution> colliders;
    private int frameCount = 0;

    public CollisionManager() {
        colliders = new HashMap<>();
    }

    public void addCollider(Entity entity, Collider collider) {
        colliders.put(entity, new ColliderEvolution(collider));
    }

    public void removeCollider(Entity entity) {
        colliders.remove(entity);
    }

    public void findCollisions(EventDispatcher dispatcher) {
        frameCount++;
        for (Map.Entry<Entity, ColliderEvolution> e1: colliders.entrySet()) {
            if (!e1.getValue().before.isMobile()) continue;

            List<Map.Entry<Entity, ColliderEvolution>> sortedCollisions = colliders.entrySet().stream()
                    .filter(e2 -> !e2.getKey().equals(e1.getKey()))
                    .filter(e2 -> e2.getValue().after.intersects(e1.getValue().after))
                    .sorted((a, b) -> Float.compare(b.getValue().after.intersectionArea(e1.getValue().after), a.getValue().after.intersectionArea(e1.getValue().after)))
                    .toList();

            Collider updatedCollider = e1.getValue().after;
            for (Map.Entry<Entity, ColliderEvolution> e2: sortedCollisions) {
                Optional<Vec2f> optMtv = updatedCollider.minimumTranslationVector(e1.getValue().before, e2.getValue().after);

                if (optMtv.isPresent()) {
                    Vec2f mtv = optMtv.get();
                    System.out.println("Frame " + frameCount + ", Vec: " + mtv + ", E1: " + e1 + ", E2: " + e2);
                    dispatcher.pushEvent(new CollisionEvent(e1.getKey(), e2.getKey(), mtv));


                    if (updatedCollider instanceof RectangleCollider(Vec2f c2, _, _, _)) {
                        Vec2f updatedPosition = c2.add(mtv);
                        updatedCollider = updatedCollider.moveToPosition(updatedPosition);
                    } else {
                        throw new IllegalStateException("Unknown collider");
                    }
                }
            }
        }
    }
    @Override
    public void onEvent(EventDispatcher dispatcher, Event event) {
        switch (event) {
            case EntityMovedEvent(Entity entity, Vec2f position) -> {
                ColliderEvolution evolution = colliders.get(entity);
                if (evolution != null) {
                    evolution.setAfter(evolution.before.moveToPosition(position));
                }
            }
            case EndUpdateEvent() -> colliders.values().forEach(ColliderEvolution::evolve);
            default -> { }
        }
    }

    private static class ColliderEvolution {
        public Collider before;
        public Collider after;

        public ColliderEvolution(Collider before) {
            this.before = before;
            this.after = before;
        }

        public void evolve() {
            before = after;
        }

        public void setAfter(Collider after) {
            this.after = after;
        }
    }
}
