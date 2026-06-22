package com.game;

import com.game.entity.Entity;
import com.game.entity.EntityId;
import com.game.entity.EntityManager;
import com.game.event.bus.EventBus;
import com.game.event.bus.EventObserver;
import com.game.event.instant.RenderRequestEvent;
import com.game.renderer.texture.Texture;
import com.game.transform.Transform2D;

public class MapElementEntity implements Entity {
    private final EntityId id;
    private final Transform2D transform;
    private final Texture texture;

    private final EventObserver<RenderRequestEvent> onRenderFunc = this::onRender;

    public MapElementEntity(Transform2D transform, Texture texture, EventBus bus, EntityManager entityManager) {
        this.transform = transform;
        this.texture = texture;

        bus.addObserver(RenderRequestEvent.class, onRenderFunc);

        this.id = entityManager.registerEntity(this);
    }

    @Override
    public EntityId id() {
        return id;
    }

    public void onRender(EventBus bus, RenderRequestEvent event) {
        event.renderer().submit(transform, texture);
    }

    @Override
    public void delete(EventBus bus) {
        bus.removeObserver(RenderRequestEvent.class, onRenderFunc);
    }
}
