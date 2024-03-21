package com.example.amazingmaze.controllers;

import com.example.amazingmaze.services.GameService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/game")
@AllArgsConstructor
public class GameController {
    private final GameService gameService;

    public ResponseEntity<String> startGame(@RequestParam String username,
                                            @RequestParam int size,
                                            @RequestParam int complexity) {
        String sessionId = gameService.startNewGame(username, size, complexity);
        return ResponseEntity.ok("Игра началась, ID сессии: " + sessionId);
    }
}