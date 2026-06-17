package com.game.collision;

import com.game.Entity;
import com.game.event.*;
import com.game.event.collision.CollisionEvent;
import com.game.util.Vec2f;

import java.util.*;
public class CollisionManager implements EventObserver {

    private record ColliderState(Collider before, Collider after) {
        ColliderState movedTo(Vec2f position) {
            return new ColliderState(before, after.moveToPosition(position));
        }
        ColliderState evolve() {
            return new ColliderState(after, after);
        }
    }

    private final Map<Entity, ColliderState> colliders = new HashMap<>();

    public void addCollider(Entity entity, Collider collider) {
        colliders.put(entity, new ColliderState(collider, collider));
    }

    public void removeCollider(Entity entity) {
        colliders.remove(entity);
    }

    public void findCollisions(EventDispatcher dispatcher) {
        colliders.entrySet().stream()
                .filter(e -> e.getValue().before().isMobile())
                .forEach(e -> resolveCollisionsFor(dispatcher, e));
    }

    private void resolveCollisionsFor(EventDispatcher dispatcher, Map.Entry<Entity, ColliderState> entry) {
        Vec2f beforeCenter = entry.getValue().before().center();
        Collider resolved = entry.getValue().after();

        for (var collision : sortedCollisions(resolved)) {
            Optional<Vec2f> optMtv = resolved.minimumTranslationVector(beforeCenter, collision.getValue().after());
            if (optMtv.isEmpty()) continue;

            Vec2f mtv = optMtv.get();
            dispatcher.pushEvent(new CollisionEvent(entry.getKey(), collision.getKey(), mtv));
            resolved = resolved.moveToPosition(resolved.center().add(mtv));
        }
    }

    private List<Map.Entry<Entity, ColliderState>> sortedCollisions(Collider subject) {
        return colliders.entrySet().stream()
                .filter(e -> !e.getValue().after().equals(subject))
                .filter(e -> e.getValue().after().intersects(subject))
                .sorted(byDecreasingOverlapWith(subject))
                .toList();
    }

    private Comparator<Map.Entry<Entity, ColliderState>> byDecreasingOverlapWith(Collider subject) {
        return Comparator.comparingDouble(e ->
                -e.getValue().after().collisionAxes(subject)
                        .map(CollisionAxes::area)
                        .orElse(0f));
    }

    @Override
    public void onEvent(EventDispatcher dispatcher, Event event) {
        switch (event) {
            case EntityMovedEvent(Entity entity, Vec2f position) -> {
                ColliderState state = colliders.get(entity);
                if (state != null) colliders.put(entity, state.movedTo(position));
            }
            case EndUpdateEvent() -> colliders.replaceAll((_, state) -> state.evolve());
            default -> {}
        }
    }
}