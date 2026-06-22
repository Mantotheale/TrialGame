package com.game.entity;

import com.game.event.bus.EventBus;
import com.game.event.deferred.EntityDeletedEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class EntityManager {
    private final Map<EntityId, Entity> entities;

    public EntityManager(EventBus bus) {
        entities = new HashMap<>();
        bus.addObserver(EntityDeletedEvent.class, this::onEntityDeleted);
    }

    public EntityId registerEntity(Entity entity) {
        EntityId id = EntityId.generateId();
        entities.put(id, entity);
        return id;
    }

    public void deleteEntity(Entity entity) {
        entities.remove(entity.id());
    }

    public boolean isPresent(EntityId id) {
        return entities.containsKey(id);
    }

    public Entity getById(EntityId id) {
        Entity entity = entities.get(id);

        if (entity == null)
            throw new NoSuchElementException("There is no entity with ID " + id);

        return entity;
    }

    private void onEntityDeleted(EventBus bus, EntityDeletedEvent event) {
        entities.remove(event.entityId());
    }
}
