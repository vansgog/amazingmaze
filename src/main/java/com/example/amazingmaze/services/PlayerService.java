package com.example.amazingmaze.services;

import com.example.amazingmaze.model.Maze;
import com.example.amazingmaze.model.Player;
import com.example.amazingmaze.model.Session;
import com.example.amazingmaze.utils.PlayerMoveObserver;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class PlayerService {
    private final SessionService sessionService;
    private final CopyOnWriteArrayList<PlayerMoveObserver> observers = new CopyOnWriteArrayList<>();

    public PlayerService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    public boolean movePlayer(String sessionId, @NotNull String direction) {
        Session session = sessionService.getSession(sessionId);
        if (session == null) {
            log.info("Сессия не найдена");
            return false;
        }
        Player player = session.getPlayer();
        int xNew = player.getX();
        int yNew = player.getY();
        switch (direction.toLowerCase()) {
            case "w": yNew -= 1; break;
            case "s": yNew += 1; break;
            case "a": xNew -= 1; break;
            case "d": xNew += 1; break;
            default: return false;
        }
        if (canMove(xNew, yNew, sessionId)) {
            player.setX(xNew);
            player.setY(yNew);
            notifyObservers();
            return true;
        } else {
            log.info("Движение в этом направлении невозможно");
            return false;
        }
    }

    private boolean canMove(int x, int y, String sessionId) {
        Maze maze = sessionService.getSession(sessionId).getMaze();
        if (x < 0 ||
            y < 0 ||
            x > (sessionService
                    .getSession(sessionId)
                    .getMaze()
                    .getMaze().length - 1) ||
            y > (maze.getMaze()[0].length - 1)) {
            return false;
        }
        return maze.getMaze()[y][x] != Maze.WALL;
    }

    public void addObserver(PlayerMoveObserver observer) {
        observers.add(observer);
    }

    public void notifyObservers() {
        observers.forEach(PlayerMoveObserver::onPlayerMoved);
    }
}