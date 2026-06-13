package com.game.input;

public interface InputObservable {
    void addObserver(InputObserver observer);
    void removeObserver(InputObserver observer);
    void notifyObservers(Input input);
}
