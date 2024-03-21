package com.example.amazingmaze.controllers;

import com.example.amazingmaze.model.Maze;
import com.example.amazingmaze.model.Session;
import com.example.amazingmaze.services.SessionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/game/{sessionId}/maze")
@AllArgsConstructor
public class MazeController {
    private final SessionService sessionService;

    @GetMapping("/current")
    public ResponseEntity<Maze> getCurrentMaze(@PathVariable String sessionId) {
        Session session = sessionService.getSession(sessionId);
        if (session == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(session.getMaze());
    }
}