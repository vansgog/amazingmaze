package com.example.amazingmaze.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class SessionForMultiplay {
    private Maze maze;
    private final Map<UUID, Player> players;
    private final LocalDateTime startTime;
    private LocalDateTime endTime;
    private final int complexity;
    private final int size;
    private int score;

    public SessionForMultiplay(Maze maze, int complexity, int size) {
        this.maze = maze;
        this.players = new ConcurrentHashMap<>();
        this.complexity = complexity;
        this.size = size;
        this.startTime = LocalDateTime.now();
    }

//    public void addPlayer(Player player) {
//        this.players.put(player.getId(), player);
//    }

    public void removePlayer(UUID playerId) {
        this.players.remove(playerId);
    }

    public Collection<Player> getPlayers() {
        return this.players.values();
    }

    public void endSession() {
        this.endTime = LocalDateTime.now();
    }
}