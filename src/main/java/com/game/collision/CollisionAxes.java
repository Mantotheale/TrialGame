package com.game.collision;

public record CollisionAxes(float xOverlap, float yOverlap) {
    public float area() { return xOverlap * yOverlap; }
}