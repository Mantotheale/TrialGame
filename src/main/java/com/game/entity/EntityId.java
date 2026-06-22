package com.game.entity;

public class EntityId {
    private static int counter = 1;

    private final int id;

    private EntityId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return (obj instanceof EntityId other) && this.id == other.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "EntityID(" + id + ")";
    }

    static EntityId generateId() {
        EntityId id = new EntityId(counter);
        counter++;
        return id;
    }

    public final static EntityId NONE = new EntityId(0);
}
