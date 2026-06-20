package com.game.collision;

import com.game.Entity;
import com.game.event.*;
import com.game.event.deferred.EntityMovedEvent;
import com.game.event.bus.EventBus;
import com.game.event.bus.EventObserver;
import com.game.math.Rectangle;
import com.game.math.Vec2f;

import java.util.*;
public class CollisionManager {

    /*private record ColliderState(Collider before, Collider after) {
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

    public void findCollisions(EventBus dispatcher) {
        for (var e: colliders.entrySet())
            if (e.getValue().before.isMobile())
                resolveCollisionsFor(dispatcher, e);
    }

    private void resolveCollisionsFor(EventBus dispatcher, Map.Entry<Entity, ColliderState> entry) {
        Vec2f beforeCenter = entry.getValue().before().center();
        Collider resolved = entry.getValue().after();

        int c = 0;
        for (var collision : sortedCollisions(entry.getKey(), resolved)) {
            Rectangle thisRect = ((RectangleCollider)entry.getValue().before).rectangle();
            Rectangle collRect = ((RectangleCollider) collision.getValue().after).rectangle();
            System.out.println("----");
            System.out.println("Before " + entry.getValue().before);
            System.out.println("After " + entry.getValue().after);
            System.out.println("Collider " + collision.getValue().after);
            Optional<Float> collTime = thisRect.intersectionTime(collRect, entry.getValue().after.center());
            System.out.println("Collision time " + collTime);
            collTime.ifPresent(aFloat -> System.out.println("Collision state " + thisRect.stopAtTime(entry.getValue().after.center(), aFloat)));
            System.out.println("----");

            Optional<Vec2f> optMtv = resolved.minimumTranslationVector(beforeCenter, collision.getValue().after());
            if (optMtv.isEmpty()) continue;

            System.out.println("collision number " + c + ", mtv: " + optMtv);
            c++;
            Vec2f mtv = optMtv.get();
            dispatcher.pushEvent(new CollisionEvent(entry.getKey(), collision.getKey(), mtv));
            resolved = resolved.moveToPosition(resolved.center().add(mtv));
            System.out.println("new pos " + resolved);
        }
    }

    private List<Map.Entry<Entity, ColliderState>> sortedCollisions(Entity self, Collider subject) {
        return colliders.entrySet().stream()
                .filter(e -> !e.getKey().equals(self))
                .filter(e -> e.getValue().after().intersects(subject))
                .sorted(byDecreasingOverlapWith(subject))
                .toList();
    }

    private Comparator<Map.Entry<Entity, ColliderState>> byDecreasingOverlapWith(Collider subject) {
        return Comparator.comparingDouble(e ->
                -subject.overlapArea(e.getValue().after()));
    }

    @Override
    public void onEvent(EventBus dispatcher, Event event) {
        switch (event) {
            case EntityMovedEvent(Entity entity, Vec2f position) -> {
                ColliderState state = colliders.get(entity);
                if (state != null) colliders.put(entity, state.movedTo(position));
            }
            case EndUpdateEvent() -> colliders.replaceAll((_, state) -> state.evolve());
            default -> {}
        }
    }*/
}