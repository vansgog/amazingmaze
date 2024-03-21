package com.example.amazingmaze.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Minotaur {
    private int x, y;
    private boolean alive;
    private int prevX = -1;
    private int prevY = -1;

    public Minotaur(int x, int y) {
        this.x = x;
        this.y = y;
        this.alive = true;
    }

    public void kill() {
        this.alive = false;
    }
}