package com.example.amazingmaze.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class Game implements Serializable {
    private final String sessionId;
    private final int x;
    private final int y;
    private final long pauseTime;
}