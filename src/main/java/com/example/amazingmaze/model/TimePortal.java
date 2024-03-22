package com.example.amazingmaze.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
@Getter
@Setter
public class TimePortal {
    private int x;
    private int y;
    private AtomicBoolean isActive;

    public TimePortal(int x, int y, boolean isActive) {
        this.x = x;
        this.y = y;
        this.isActive = new AtomicBoolean(isActive);
    }

    public boolean isActive() {
        return isActive.get();
    }

    public void setActive(boolean active) {
        isActive.set(active);
    }
}