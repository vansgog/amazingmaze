package com.example.amazingmaze.services;

import com.example.amazingmaze.model.*;
import com.example.amazingmaze.repositories.GameRepository;
import com.example.amazingmaze.utils.MinotaurManager;
import com.example.amazingmaze.utils.PortalManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GameService {
    @Autowired
    private PlayerService playerService;
    @Autowired
    private MazeService mazeService;
    private final GameRepository gameRepository;
    @Autowired
    private SessionService sessionService;
    private String currentSessionId;
    private PortalManager portalManager;
    private MinotaurManager minotaurManager;
    private final Map<String, Game> pausedGames = new HashMap<>();

    private Maze maze;
    private Player player;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public void gatherInfo() {
        Scanner scanner = new Scanner(System.in);
        log.info("Введите свой ник: ");
        String username = scanner.nextLine();
        log.info("Выберите размер лабиринта (10, 20, 30): ");
        int selectedSize = scanner.nextInt();
        selectedSize = Math.max(10, Math.min(selectedSize, 30));
        log.info("Выберите сложность от 1 до 10: ");
        int selectedComplexity = scanner.nextInt();
        selectedComplexity = Math.max(1, Math.min(selectedComplexity, 10));
        startNewGame(username, selectedSize, selectedComplexity);
    }

    public String startNewGame(String username, int size, int complexity) {
        this.maze = new Maze(size);
        mazeService.initializeMaze(maze);
        mazeService.generateSomeMaze(maze, 1, 1);
        mazeService.createTrapsAndBypasses(maze);
        mazeService.setExitBasedOnComplexity(maze, complexity);
        this.player = new Player(username, 1, 1);
        this.currentSessionId = sessionService.createSession(maze, player, complexity, size);
        this.portalManager = new PortalManager(sessionService, mazeService, currentSessionId);
        portalManager.startPortals(size, complexity);
        this.minotaurManager = new MinotaurManager(sessionService, currentSessionId, mazeService, maze, complexity);
        displayMaze();
        sessionService.getSession(currentSessionId).setStartTime(LocalDateTime.now());
        startGameLoop(this.currentSessionId);
        return currentSessionId;
    }

    @SneakyThrows
    public void startGameLoop(String currentSessionId) {
        Scanner scanner = new Scanner(System.in);
        boolean isGameRunning = true;
        while (isGameRunning) {
            log.info("Введите команду (W, A, S, D, exit): ");
            String command = scanner.next().toLowerCase();
            switch (command) {
                case "w", "a", "s", "d" :
                      playerService.movePlayer(currentSessionId, command);
                      if (mazeService.isPortalPresent(maze, player.getX(), player.getY())) teleportPlayer();
                      if (minotaurManager.checkIfCaughtPlayer()) {
                          log.info("Минотавр убил Вас, Вы проиграли :(");
                          finishGame(currentSessionId);
                          isGameRunning = false;
                      }
                      if (isExitReached(player.getX(), player.getY(), currentSessionId)) {
                          log.info("Вы нашли выход!");
                          finishGame(currentSessionId);
                          isGameRunning = false;
                      }
                    displayMaze();
                    break;
                case "pause" :
                    pauseGame(currentSessionId);
                    while (!scanner.nextLine().equalsIgnoreCase("resume")) log.info("Введите 'resume'");
                    resumeGame(currentSessionId);
                    break;
                case "exit" :
                    isGameRunning = false;
                    finishGame(currentSessionId);
                    break;
                default:
                    log.info("Неверная команда");
                    break;
            }
        }
        scanner.close();
    }

    private void pauseGame(String currentSessionId) {
        saveCurrentGameState(currentSessionId);
        sessionService.addPauseTime(currentSessionId, LocalDateTime.now());
        portalManager.stopPortals();
        log.info("Работа порталов приостановлена");
        minotaurManager.pauseMinotaurMovement();
        log.info("Игра приостановлена. Введите 'resume' чтобы продолжить");
    }

    public void saveCurrentGameState(String sessionId) {
        Session session = sessionService.getSession(sessionId);
        if (session != null) {
            player = session.getPlayer();
            Game game = new Game(sessionId, player.getX(), player.getY(), System.currentTimeMillis());
            pausedGames.put(sessionId, game);
            log.info("Игра сохранена");
        }
    }

    public void resumeGame(String currentSessionId) {
        Game game = pausedGames.get(currentSessionId);
        if (game != null) {
            this.currentSessionId = currentSessionId;
            Session session = sessionService.getSession(currentSessionId);
            if (session != null) {
                sessionService.addResumeTime(currentSessionId, LocalDateTime.now());
                session.getPlayer().setX(game.getX());
                session.getPlayer().setY(game.getY());
                log.info("Игра возобновлена");
                portalManager.resumePortals();
                minotaurManager.resumeMinotaurMovement();
                displayMaze();
                startGameLoop(currentSessionId);
            }
        }
    }

    private void teleportPlayer() {
        List<TimePortal> activePortals = maze
                                            .getPortals()
                                            .stream()
                                            .filter(TimePortal::isActive)
                                            .collect(Collectors.toList());
        activePortals.removeIf(portal -> portal.getX() == player.getX() &&
                                         portal.getY() == player.getY());
        if (!activePortals.isEmpty()) {
            TimePortal destinationPortal = activePortals.get(new Random().nextInt(activePortals.size()));
            player.setX(destinationPortal.getX());
            player.setY(destinationPortal.getY());
        }
        if (!minotaurManager.isChasingStarted()) {
            playerService.addObserver(minotaurManager);
            minotaurManager.startChasingPlayer();
        }
    }

    private boolean isExitReached(int x, int y, String sessionId) {
        return sessionService
                .getSession(sessionId)
                .getMaze()
                .getMaze()[y][x] == Maze.EXIT;
    }

    @SneakyThrows
    public void finishGame(String currentSessionId) {
        Session session = sessionService.getSession(currentSessionId);
        portalManager.stopPortals();
        minotaurManager.stopMinotaur();
        session.setEndTime(LocalDateTime.now());
        long duration = getGameDuration(currentSessionId);
        if (isExitReached(player.getX(), player.getY(), currentSessionId)) {
            double finalScore = calculateScore(session.getSize(), session.getComplexity(), currentSessionId);
            session.setScore(finalScore);
        } else session.setScore(0);
        sessionService.endSession(currentSessionId);
        log.info("Игра завершена. Ваш результат: " + session.getScore() + ", Время: " + duration + " сек");
        while (true) {
            Scanner scanner = new Scanner(System.in);
            log.info("Начать новую игру? (Y/N): ");
            String response = scanner.nextLine().toLowerCase();
            if (response.equalsIgnoreCase("y")) gatherInfo();
            else if (response.equalsIgnoreCase("n")) {
                scanner.close();
                gameRepository.displayResults();
                break;
            } else log.info("Неверная команда");
        }
    }

    private long getGameDuration(String currentSessionId) {
        Session session = sessionService.getSession(currentSessionId);
        if (session.getStartTime() == null ||
            session.getEndTime() == null) return 0;
        long totalDuration = Duration.between(session.getStartTime(), session.getEndTime()).getSeconds();
        return totalDuration - session.getTotalPausedDuration();
    }

    private double calculateScore(int size, int complexity, String currentSessionId) {
        long gameDuration = getGameDuration(currentSessionId);
        if (gameDuration == 0) return 0;
        return (double) (size * complexity) / gameDuration * 100;
    }

    private void displayMaze() {
        Session session = sessionService.getSession(currentSessionId);
        maze = session.getMaze();
        player = session.getPlayer();
        StringBuilder sb = new StringBuilder();
        int[][] currentMaze = maze.getMaze();
        for (int i = 0; i < currentMaze.length; i++) {
            for (int j = 0; j < currentMaze[i].length; j++) {
                if (i == player.getY() && j == player.getX()) sb.append("P ");
                else if (i == minotaurManager.getMinotaur().getY() &&
                         j == minotaurManager.getMinotaur().getX()) sb.append("M ");
                else if (mazeService.isPortalPresent(maze, j, i)) sb.append("T ");
                else if (currentMaze[i][j] == Maze.OPEN_CELL) sb.append("  ");
                else if (currentMaze[i][j] == Maze.WALL) sb.append("# ");
                else if (currentMaze[i][j] == Maze.EXIT) sb.append("E ");
            }
            sb.append("\n");
        }
        log.info("\n" + sb);
    }
}