package com.game.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class EntityManager {
    private final Map<EntityId, Entity> entities;

    public EntityManager() {
        entities = new HashMap<>();
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
}
