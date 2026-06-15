package com.game;

import com.game.collision.CollisionManager;
import com.game.collision.RectangleCollider;
import com.game.event.*;
import com.game.input.InputManager;
import com.game.input.rawcomponents.KeyState;
import com.game.input.rawcomponents.PhysicalKey;
import com.game.renderer.Renderer;
import com.game.renderer.texture.Texture;
import com.game.transform.Transform2D;
import com.game.util.Vec2f;

public class Reshiram extends Entity implements EventObserver {
    private final InputManager inputManager;

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
        );
    }

    @Override
    public void onEvent(EventDispatcher dispatcher, Event event) {
        switch (event) {
            case StartUpdateEvent() -> {
                boolean hasMoved = false;

                if (inputManager.keyState(PhysicalKey.W) == KeyState.DOWN) {
                    transform = transform.translate(0, 1 * (float) Game.UPDATE_TIME);
                    hasMoved = true;
                }
                if (inputManager.keyState(PhysicalKey.S) == KeyState.DOWN) {
                    transform = transform.translate(0, -1 * (float) Game.UPDATE_TIME);
                    hasMoved = true;
                }
                if (inputManager.keyState(PhysicalKey.A) == KeyState.DOWN) {
                    transform = transform.translate(-1 * (float) Game.UPDATE_TIME, 0);
                    hasMoved = true;
                }
                if (inputManager.keyState(PhysicalKey.D) == KeyState.DOWN) {
                    transform = transform.translate(1 * (float) Game.UPDATE_TIME, 0);
                    hasMoved = true;
                }

                if (hasMoved)
                    dispatcher.pushEvent(new EntityMovedEvent(this, transform.translation().toVec2f()));
            }
            case RenderRequestEvent(Renderer renderer) -> renderer.submit(transform, texture);
            case CollisionEvent(Entity e1, Entity e2, Vec2f minimumTranslationVector) when e1.equals(this) -> {
                System.out.println("Collision with " + e2 + "!");
                transform = transform.translate(minimumTranslationVector);
                if (!minimumTranslationVector.equals(Vec2f.ZERO))
                    dispatcher.pushEvent(new EntityMovedEvent(this, transform.translation().toVec2f()));
            }
            default -> { }
        }
    }
}
