package com.game;

import com.game.collision.CollisionManager;
import com.game.event.InstantEvent;
import com.game.event.bus.EventBus;
import com.game.event.bus.EventObserver;
import com.game.event.deferred.EntityMovedEvent;
import com.game.event.instant.RenderRequestEvent;
import com.game.event.instant.UpdateEvent;
import com.game.input.InputManager;
import com.game.input.rawcomponents.KeyState;
import com.game.input.rawcomponents.PhysicalKey;
import com.game.math.Vec2f;
import com.game.renderer.Renderer;
import com.game.renderer.texture.Texture;
import com.game.transform.Transform2D;

public class Reshiram extends Entity implements EventObserver<InstantEvent> {
    private final InputManager inputManager;
    private final float movementSpeed = 20 * (float) Game.UPDATE_TIME;
    private Vec2f velocity = Vec2f.ZERO;

    public Reshiram(Transform2D transform, Texture texture, InputManager inputManager, CollisionManager collisionManager) {
        this.inputManager = inputManager;
        super(transform, texture);
       /* collisionManager.addCollider(
                this,
                new RectangleCollider(
                        transform.translation().toVec2f(),
                        transform.scale().compose(0.8f).toVec2f(),
                        true
                )
               //new CircleCollider(transform.translation().toVec2f(), 0.4f, true)
        );*/
    }

    @Override
    public void onEvent(EventBus bus, InstantEvent event) {
        switch (event) {
            case UpdateEvent() -> {
                Vec2f direction = Vec2f.ZERO;
                if (inputManager.keyState(PhysicalKey.W) == KeyState.DOWN)
                    direction = direction.add(Vec2f.UP);
                if (inputManager.keyState(PhysicalKey.S) == KeyState.DOWN)
                    direction = direction.add(Vec2f.DOWN);
                if (inputManager.keyState(PhysicalKey.A) == KeyState.DOWN)
                    direction = direction.add(Vec2f.LEFT);
                if (inputManager.keyState(PhysicalKey.D) == KeyState.DOWN)
                    direction = direction.add(Vec2f.RIGHT);

                if (!direction.equals(Vec2f.ZERO)) {
                    Vec2f velocity = direction.normalize().mul(movementSpeed);
                    transform = transform.translate(velocity);
                    bus.postDeferredEvent(new EntityMovedEvent(this, transform.translation().toVec2f()));
                }
            }
            case RenderRequestEvent(Renderer renderer) -> renderer.submit(transform, texture);
            default -> { }
        }
    }

}
