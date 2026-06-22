package com.game;

import com.game.collision.CollisionManager;
import com.game.event.*;
import com.game.event.bus.EventBus;
import com.game.event.instant.RenderRequestEvent;
import com.game.event.instant.UpdateEvent;
import com.game.renderer.Renderer;
import com.game.renderer.texture.Texture;
import com.game.renderer.texture.Tile;
import com.game.resourcemanager.ResourceManager;
import com.game.transform.Scale2D;
import com.game.transform.Transform2D;

public class MewTwo extends Entity {
    private int frames;
    private final ResourceManager resourceManager;

    public MewTwo(Transform2D transform, Texture texture, ResourceManager resourceManager, CollisionManager collisionManager) {
        super(transform, texture);
        this.resourceManager = resourceManager;
        /*collisionManager.addCollider(
                this,
                new CircleCollider(transform.translation().toVec2f(), 0.4f, false)
                //new RectangleCollider(transform.translation().toVec2f(), transform.scale().compose(0.8f).toVec2f(), false)
        );*/
        frames = 0;
    }

    @Override
    public void onEvent(EventBus dispatcher, InstantEvent event) {
        switch (event) {
            case UpdateEvent() -> {
                frames++;

                if (frames == 60) {
                    Bullet bullet = new Bullet(
                            new Transform2D(
                                    transform.translate(-1, 0).translation(),
                                    Scale2D.UNIT,
                                    3
                            ),
                            resourceManager.getTexture(Tile.BULLET)
                    );
                    dispatcher.addObserver(RenderRequestEvent.class, bullet);
                    dispatcher.addObserver(bullet);
                    frames = 0;
                }
            }
            case RenderRequestEvent(Renderer renderer) -> renderer.submit(transform, texture);
            default -> { }
        }
    }
}
