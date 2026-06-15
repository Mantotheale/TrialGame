package com.game;

import com.game.event.*;
import com.game.renderer.Renderer;
import com.game.renderer.texture.Texture;
import com.game.renderer.texture.Tile;
import com.game.resourcemanager.ResourceManager;
import com.game.transform.Scale2D;
import com.game.transform.Transform2D;

public class MewTwo extends Entity {
    private int frames;
    private final ResourceManager resourceManager;

    public MewTwo(Transform2D transform, Texture texture, ResourceManager resourceManager) {
        super(transform, texture);
        this.resourceManager = resourceManager;
        frames = 0;
    }

    @Override
    public void onEvent(EventDispatcher dispatcher, Event event) {
        switch (event) {
            case StartUpdateEvent() -> {
                frames++;

                if (frames == 60) {
                    System.out.println("Fire!");
                    Bullet bullet = new Bullet(
                            new Transform2D(
                                    transform.translate(-1, 0).translation(),
                                    Scale2D.UNIT,
                                    3
                            ),
                            resourceManager.getTexture(Tile.BULLET)
                    );
                    dispatcher.addObserver(bullet);
                    frames = 0;
                }
            }
            case RenderRequestEvent(Renderer renderer) -> renderer.submit(transform, texture);
            default -> { }
        }
    }
}
