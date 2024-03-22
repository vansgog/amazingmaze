package com.example.amazingmaze.model;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Minotaur {
    private AtomicInteger x, y;
    private AtomicBoolean alive;

    @Getter
    @Setter
    private int prevX = -1;
    @Getter
    @Setter
    private int prevY = -1;

    public Minotaur(int x, int y) {
        this.x = new AtomicInteger(x);
        this.y = new AtomicInteger(y);
        this.alive = new AtomicBoolean(true);
    }

    public int getX() {
        return x.get();
    }

    public void setX(int newX) {
        x.set(newX);
    }

    public int getY() {
        return y.get();
    }

    public void setY(int newY) {
        y.set(newY);
    }

    public boolean isAlive() {
        return alive.get();
    }

    public void kill() {
        alive.set(false);
    }
}