package com.example.amazingmaze.utils;

import com.example.amazingmaze.model.Maze;
import com.example.amazingmaze.model.Session;
import com.example.amazingmaze.services.MazeService;
import com.example.amazingmaze.services.SessionService;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PortalManager {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final MazeService mazeService;
    private final SessionService sessionService;
    private final String currentSessionId;

    public PortalManager(SessionService sessionService, MazeService mazeService, String currentSessionId) {
        this.sessionService = sessionService;
        this.mazeService = mazeService;
        this.currentSessionId = currentSessionId;
    }

    public void start(int timePeriod) {
        scheduler.scheduleAtFixedRate(this::managePortals, 10, timePeriod, TimeUnit.SECONDS);
    }

    private void managePortals() {
        Session session = sessionService.getSession(currentSessionId);
        Maze currentMaze = session.getMaze();
        if (new Random().nextBoolean()) {
            mazeService.addPortal(currentMaze);
            mazeService.addPortal(currentMaze);
        } else mazeService.removeRandomPortal(currentMaze);
    }

    public void stop() {
        if (!scheduler.isShutdown()) scheduler.shutdown();
    }
}