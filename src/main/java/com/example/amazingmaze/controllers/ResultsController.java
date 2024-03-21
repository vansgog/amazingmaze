package com.example.amazingmaze.controllers;

import com.example.amazingmaze.repositories.GameRepository;
import com.example.amazingmaze.utils.GameResult;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/game/results")
@AllArgsConstructor
public class ResultsController {
    private final GameRepository gameRepository;

    @GetMapping("/all")
    public ResponseEntity<List<GameResult>> displayAllResults() {
        List<GameResult> results = gameRepository.findAllResults();
        if (results.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(results);
    }
}