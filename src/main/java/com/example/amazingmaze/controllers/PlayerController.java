package com.example.amazingmaze.controllers;

import com.example.amazingmaze.services.PlayerService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/game/{sessionId}/player")
@AllArgsConstructor
public class PlayerController {
    private final PlayerService playerService;

    public ResponseEntity<String> movePlayer(@PathVariable String sessionId,
                                             @RequestParam String direction) {
        boolean success = playerService.movePlayer(sessionId, direction);
        if (success) return ResponseEntity.ok("Игрок успешно перемещен");
        return ResponseEntity.badRequest().body("Перемещение невозможно");
    }
}