package com.example.amazingmaze.utils;

import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Table(name = "gameresult")
public class GameResult {
    @Column(name = "username")
    private String username;
    @Column(name = "size")
    private int size;
    @Column(name = "complexity")
    private int complexity;
    @Column(name = "duration")
    private long duration;
    @Column(name = "score")
    private int score;
}