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
    private ScheduledExecutorService portalScheduler;
    private final MazeService mazeService;
    private final SessionService sessionService;
    private final String currentSessionId;

    private int savedTimePeriod;
    private int savedComplexity;

    public PortalManager(SessionService sessionService, MazeService mazeService, String currentSessionId) {
        this.sessionService = sessionService;
        this.mazeService = mazeService;
        this.currentSessionId = currentSessionId;
        portalScheduler = Executors.newScheduledThreadPool(1);
    }

    public void startPortals(int timePeriod, int complexity) {
        this.savedTimePeriod = timePeriod;
        this.savedComplexity = complexity;
        portalScheduler.scheduleAtFixedRate(() -> managePortals(complexity), 10, timePeriod, TimeUnit.SECONDS);
    }

    public void resumePortals() {
        if (portalScheduler.isShutdown() || portalScheduler.isTerminated()) {
            portalScheduler = Executors.newScheduledThreadPool(1);
            startPortals(savedTimePeriod, savedComplexity);
            log.info("Работа порталов возобновлена");
        }
    }

    private void managePortals(int complexity) {
        Session session = sessionService.getSession(currentSessionId);
        Maze currentMaze = session.getMaze();
        if (new Random().nextBoolean()) {
            for (int i = 0; complexity <= 5 ? i < (10 / complexity) :
                                              i < (20 / complexity); i++) {
                mazeService.addPortal(currentMaze);
            }
        } else mazeService.removeRandomPortal(currentMaze);
    }

    public void stopPortals() {
        if (!portalScheduler.isShutdown()) portalScheduler.shutdownNow();
    }
}