package com.example.amazingmaze.services;

import com.example.amazingmaze.model.Maze;
import com.example.amazingmaze.model.Player;
import com.example.amazingmaze.model.Session;
import com.example.amazingmaze.repositories.GameRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class SessionService {
    private final Map<String, Session> sessions = new HashMap<>();
    private final GameRepository gameRepository;

    public SessionService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public String createSession(Maze maze, Player player, int complexity, int size) {
        Session session = new Session(maze, player, complexity, size);
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, session);
        return sessionId;
    }

    public Session getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public void endSession(String sessionId) {
        Session session = getSession(sessionId);
        if (session != null && !session.isEnded()) {
            session.endSession();
            gameRepository.saveGameResult(session.getPlayer().getUsername(),
                                          session.getSize(),
                                          session.getComplexity(),
                                          Duration.between(session.getStartTime(),
                                                           session.getEndTime()).getSeconds(),
                                          session.getScore());
            session.setEnded(true);
        }
    }

    public void addPauseTime(String sessionId, LocalDateTime pauseTime) {
        sessions.get(sessionId).getPauseTimes().add(pauseTime);
    }

    public void addResumeTime(String sessionId, LocalDateTime resumeTime) {
        Session session = sessions.get(sessionId);
        if (session != null) {
            session.getResumeTimes().add(resumeTime);
            if (session.getPauseTimes().size() == session.getResumeTimes().size()) {
                long totalPausedDuration = 0;
                for (int i = 0; i < session.getPauseTimes().size(); i++) {
                    totalPausedDuration += ChronoUnit.SECONDS.between(session.getPauseTimes().get(i),
                                                                      session.getResumeTimes().get(i));
                }
                session.addTotalPausedDuration(totalPausedDuration);
                session.getPauseTimes().clear();
                session.getResumeTimes().clear();
            }
        }
    }
}