package com.example.amazingmaze.model;

import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Table(name = "session")
public class Session {
    private final Maze maze;
    private final Player player;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<LocalDateTime> pauseTimes = new ArrayList<>();
    private List<LocalDateTime> resumeTimes = new ArrayList<>();
    private long totalPausedDuration = 0;
    private final int complexity;
    private final int size;
    private int score;
    private boolean ended = false;

    public Session(Maze maze, Player player, int complexity, int size) {
        this.maze = maze;
        this.player = player;
        this.complexity = complexity;
        this.size = size;
    }

    public void endSession() {
        this.endTime = LocalDateTime.now();
    }

    public void addTotalPausedDuration(long seconds) {
        this.totalPausedDuration += seconds;
    }
}