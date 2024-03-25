package com.example.amazingmaze.utils;

import com.example.amazingmaze.model.Maze;
import com.example.amazingmaze.model.Minotaur;
import com.example.amazingmaze.model.Player;
import com.example.amazingmaze.model.Session;
import com.example.amazingmaze.services.MazeService;
import com.example.amazingmaze.services.SessionService;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MinotaurManager implements PlayerMoveObserver {
    private final MazeService mazeService;
    private final SessionService sessionService;
    private final String sessionId;
    private final Maze maze;
    private final int complexity;
    @Getter
    private final Minotaur minotaur;
    private ScheduledExecutorService minotaurScheduler;
    @Getter
    private CopyOnWriteArrayList<int[]> pathToPlayer = new CopyOnWriteArrayList<>();
    @Getter
    private boolean chasingStarted = false;
    private static final long INITIAL_DELAY = 0;

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
        this.minotaur = new Minotaur(maze.getCols() - 2, maze.getRows() - 2);
        long movementInterval = 5_000 / this.complexity;
        initializeMovementScheduler(movementInterval);
    }

    private void initializeMovementScheduler(long interval) {
        if (minotaurScheduler != null && !minotaurScheduler.isShutdown()) {
            minotaurScheduler.shutdownNow();
        }
        minotaurScheduler = Executors.newScheduledThreadPool(1);
        minotaurScheduler.scheduleWithFixedDelay(() -> {
            if (!chasingStarted) moveMinotaurRandomly();
            else continueChasingPlayer();
        }, INITIAL_DELAY, interval, TimeUnit.MILLISECONDS);
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
        if (!chasingStarted) {
            chasingStarted = true;
            long chasingInterval = 4_000 / complexity;
            initializeMovementScheduler(chasingInterval);
            updatePlayerPosition();
        }
    }

    private void continueChasingPlayer() {
        if (!pathToPlayer.isEmpty()) {
            int[] nextStep = pathToPlayer.remove(0);
            minotaur.setX(nextStep[0]);
            minotaur.setY(nextStep[1]);
            checkIfCaughtPlayer();
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
        }
    }

    @Override
    public void onPlayerMoved() {
        updatePlayerPosition();
    }

    public boolean checkIfCaughtPlayer() {
        int[] playerPosition = getPlayerPosition();
        if (minotaur.getX() == playerPosition[0] &&
            minotaur.getY() == playerPosition[1]) {
            stopMinotaur();
            return true;
        }
        return false;
    }

    public void pauseMinotaurMovement() {
        if (minotaurScheduler != null && !minotaurScheduler.isShutdown()) {
            minotaurScheduler.shutdownNow();
            log.info("Движение минотавра приостановлено");
        }
    }

    public void resumeMinotaurMovement() {
        long currentInterval = chasingStarted ? (4_000 / complexity) : (5_000 / complexity);
        initializeMovementScheduler(currentInterval);
        log.info("Движение минотавра возобновлено");
    }

    @SneakyThrows
    public void stopMinotaur() {
        if (minotaurScheduler != null) {
            minotaurScheduler.shutdown();
            if (!minotaurScheduler.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                minotaurScheduler.shutdownNow();
            }
        }
    }
}