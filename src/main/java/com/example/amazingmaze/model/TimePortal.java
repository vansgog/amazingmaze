package com.example.amazingmaze.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class TimePortal {
    private int x;
    private int y;
    private boolean isActive;

    public TimePortal(int x, int y, boolean isActive) {
        this.x = x;
        this.y = y;
        this.isActive = isActive;
    }
}