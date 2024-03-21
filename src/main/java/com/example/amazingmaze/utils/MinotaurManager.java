package com.example.amazingmaze.utils;

import com.example.amazingmaze.model.Maze;
import com.example.amazingmaze.model.Minotaur;
import com.example.amazingmaze.model.Player;
import com.example.amazingmaze.model.Session;
import com.example.amazingmaze.services.MazeService;
import com.example.amazingmaze.services.SessionService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class MinotaurManager implements PlayerMoveObserver {
    private final MazeService mazeService;
    private final SessionService sessionService;
    private final String sessionId;
    private final Maze maze;
    private final int complexity;
    @Getter
    private Minotaur minotaur;
    private Timer minotaurTimer;
    @Getter
    private CopyOnWriteArrayList<int[]> pathToPlayer = new CopyOnWriteArrayList<>();

    public MinotaurManager(SessionService sessionService,
                           String sessionId,
                           MazeService mazeService,
                           Maze maze,
                           int complexity) {
        this.sessionService = sessionService;
        this.sessionId = sessionId;
        this.mazeService = mazeService;
        this.maze = maze;
        this.complexity = complexity;
        initializeMinotaur();
    }

    private void initializeMinotaur() {
        this.minotaur = new Minotaur(maze.getCols() - 2, maze.getRows() - 2);
        this.minotaurTimer = new Timer();
        startMinotaurMovement();
    }

    private void startMinotaurMovement() {
        minotaurTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (minotaur.isAlive()) moveMinotaurRandomly();
            }
        }, 0, 5_000 / complexity);
    }

    private void moveMinotaurRandomly() {
        List<int[]> directions = mazeService.getAvailableDirections(maze,
                                                                    minotaur.getX(),
                                                                    minotaur.getY(),
                                                                    false);
        directions.removeIf(dir -> (dir[0] == minotaur.getPrevY()) &&
                                   (dir[1] == minotaur.getPrevX()) &&
                                    directions.size() > 1);
        if (!directions.isEmpty()) {
            int[] nextStep = directions.size() == 1 ? directions.get(0) :
                                                      directions.get(new Random().nextInt(directions.size()));
            minotaur.setPrevX(minotaur.getX());
            minotaur.setPrevY(minotaur.getY());
            minotaur.setX(nextStep[1]);
            minotaur.setY(nextStep[0]);
        }
    }

    public void startChasingPlayer() {
        int[] playerPosition = getPlayerPosition();
        pathToPlayer = mazeService.findPath(maze, minotaur.getX(), minotaur.getY(),
                                            playerPosition[0], playerPosition[1]);
        continueChasingPlayer();
    }

    private void continueChasingPlayer() {
        if (!pathToPlayer.isEmpty()) {
            int[] nextStep = pathToPlayer.remove(0);
            minotaur.setX(nextStep[0]);
            minotaur.setY(nextStep[1]);
        }
    }

    public int[] getPlayerPosition() {
        Session currentSession = sessionService.getSession(sessionId);
        if (currentSession == null) throw new IllegalStateException("Текущая сессия не найдена");
        Player player = currentSession.getPlayer();
        if (player == null) throw new IllegalStateException("Игрок в текущей сессии не найден");
        return new int[]{player.getX(), player.getY()};
    }

    public void updatePlayerPosition() {
        int[] playerPosition = getPlayerPosition();
        if (pathToPlayer.isEmpty() ||
            pathToPlayer.get(pathToPlayer.size() - 1)[0] != playerPosition[0] ||
            pathToPlayer.get(pathToPlayer.size() - 1)[1] != playerPosition[1]) {
            pathToPlayer = mazeService.findPath(maze, minotaur.getX(), minotaur.getY(),
                                                playerPosition[0], playerPosition[1]);
            scheduleNextMove();
        }
    }

    @Override
    public void onPlayerMoved() {
        updatePlayerPosition();
    }

    private void scheduleNextMove() {
        if (minotaurTimer != null) minotaurTimer.cancel();
        minotaurTimer = new Timer();
        minotaurTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!pathToPlayer.isEmpty() && minotaur.isAlive()) {
                    int[] nextStep = pathToPlayer.remove(0);
                    minotaur.setX(nextStep[0]);
                    minotaur.setY(nextStep[1]);
                    checkIfCaughtPlayer();
                } else this.cancel();
            }
        }, 0, 5_000 / complexity);
    }

    private void checkIfCaughtPlayer() {
        int[] playerPosition = getPlayerPosition();
        if (minotaur.getX() == playerPosition[0] &&
            minotaur.getY() == playerPosition[1]) {
            stopMinotaur();
        }
    }

    public void stopMinotaur() {
        if (minotaurTimer != null) {
            minotaurTimer.cancel();
            minotaurTimer = null;
        }
    }
}