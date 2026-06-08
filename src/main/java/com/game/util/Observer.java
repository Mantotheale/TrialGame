package com.game.util;

public interface Observer<T> {
    void handle(T value);
}
