package com.game.event;

public sealed interface Event permits InstantEvent, DeferredEvent { }
