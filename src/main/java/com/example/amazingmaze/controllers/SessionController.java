package com.example.amazingmaze.controllers;

import com.example.amazingmaze.services.GameService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/game/session")
@AllArgsConstructor
public class SessionController {
    private final GameService gameService;

    @PostMapping("/end")
    public ResponseEntity<String> endSession(@RequestParam String sessionId) {
        gameService.finishGame(sessionId);
        return ResponseEntity.ok("Игра завершена");
    }

    @PostMapping("/pause")
    public ResponseEntity<String> pauseSession(@RequestParam String sessionId) {
        gameService.saveCurrentGameState(sessionId);
        return ResponseEntity.ok("Игра приостановлена");
    }

    @PostMapping("/resume")
    public ResponseEntity<String> resumeSession(@RequestParam String sessionId) {
        gameService.resumeGame(sessionId);
        return ResponseEntity.ok("Игра возобновлена");
    }
}