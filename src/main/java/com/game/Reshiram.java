package com.game;

import com.game.collision.Collider;
import com.game.collision.CollisionManager;
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
                new Collider(
                        new Vec2f(
                                transform.translation().x(),
                                transform.translation().y()
                        ),
                        transform.scale().x(),
                        transform.scale().y()
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
                    dispatcher.pushEvent(new EntityMovedEvent(this, new Vec2f(transform.translation().x(), transform.translation().y())));
            }
            case RenderRequestEvent(Renderer renderer) -> renderer.submit(transform, texture);
            case CollisionEvent(Entity e1, Entity e2, Collider c1, Collider c2) when e1.equals(this) -> {
                System.out.println("Collision!");
                float xOverlap = ((c1.width() + c2.width()) / 2) - Math.abs(c1.center().x() - c2.center().x());
                float yOverlap = ((c1.height() + c2.height()) / 2) - Math.abs(c1.center().y() - c2.center().y());

                if (xOverlap < yOverlap) {
                    if (c1.center().x() < c2.center().x())
                        transform = transform.translate(-xOverlap, 0);
                    else
                        transform = transform.translate(xOverlap, 0);
                } else {
                    if (c1.center().y() < c2.center().y())
                        transform = transform.translate(0, -yOverlap);
                    else
                        transform = transform.translate(0, yOverlap);
                }

                dispatcher.pushEvent(new EntityMovedEvent(this, transform.translation().toVec2f()));
            }
            default -> { }
        }
    }
}
