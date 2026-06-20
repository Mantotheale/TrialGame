package com.game.event.bus;

import com.game.event.Event;

sealed interface PendingOperation<T extends Event> permits PendingOperation.Add, PendingOperation.Remove {
    record Add<T extends Event>(EventObserver<T> observer) implements PendingOperation<T> { }
    record Remove<T extends Event>(EventObserver<T> observer) implements PendingOperation<T> { }
}