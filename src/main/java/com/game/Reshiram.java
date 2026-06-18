package com.game;

import com.game.collision.CollisionManager;
import com.game.collision.RectangleCollider;
import com.game.event.*;
import com.game.event.collision.CollisionEvent;
import com.game.input.InputManager;
import com.game.input.rawcomponents.KeyState;
import com.game.input.rawcomponents.PhysicalKey;
import com.game.renderer.Renderer;
import com.game.renderer.texture.Texture;
import com.game.transform.Transform2D;
import com.game.util.Vec2f;

public class Reshiram extends Entity implements EventObserver {
    private final InputManager inputManager;
    private final float movementSpeed = 70f * (float) Game.UPDATE_TIME;

    public Reshiram(Transform2D transform, Texture texture, InputManager inputManager, CollisionManager collisionManager) {
        this.inputManager = inputManager;
        super(transform, texture);
        collisionManager.addCollider(
                this,
                new RectangleCollider(
                        transform.translation().toVec2f(),
                        transform.scale().compose(0.8f).toVec2f(),
                        true
                )
               //new CircleCollider(transform.translation().toVec2f(), 0.4f, true)
        );
    }

    @Override
    public void onEvent(EventDispatcher dispatcher, Event event) {
        switch (event) {
            case StartUpdateEvent() -> {
                Vec2f acc = Vec2f.ZERO;
                if (inputManager.keyState(PhysicalKey.W) == KeyState.DOWN)
                    acc = acc.add(Vec2f.UP);
                if (inputManager.keyState(PhysicalKey.S) == KeyState.DOWN)
                    acc = acc.add(Vec2f.DOWN);
                if (inputManager.keyState(PhysicalKey.A) == KeyState.DOWN)
                    acc = acc.add(Vec2f.LEFT);
                if (inputManager.keyState(PhysicalKey.D) == KeyState.DOWN)
                    acc = acc.add(Vec2f.RIGHT);

                if (!acc.equals(Vec2f.ZERO)) {
                    transform = transform.translate(acc.normalize().mul(movementSpeed));
                    dispatcher.pushEvent(new EntityMovedEvent(this, transform.translation().toVec2f()));
                }
            }
            case RenderRequestEvent(Renderer renderer) -> renderer.submit(transform, texture);
            case CollisionEvent(Entity e, _, Vec2f minimumTranslationVector) when e.equals(this) -> {
                transform = transform.translate(minimumTranslationVector);
                dispatcher.pushEvent(new EntityMovedEvent(this, transform.translation().toVec2f()));
            }
            default -> { }
        }
    }
}
