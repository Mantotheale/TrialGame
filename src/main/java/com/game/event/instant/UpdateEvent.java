package com.game.event.instant;

import com.game.collision.CollisionManager;
import com.game.entity.EntityManager;
import com.game.event.InstantEvent;
import com.game.input.InputManager;
import com.game.resourcemanager.ResourceManager;

public record UpdateEvent(
        InputManager inputManager,
        ResourceManager resourceManager,
        EntityManager entityManager,
        CollisionManager collisionManager
) implements InstantEvent { }
