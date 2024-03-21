package com.example.amazingmaze.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "player")
public class Player {
    private String username;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private int id;
    private int x;
    private int y;

    public Player(String username, int xStart, int yStart) {
        this.username = username;
        this.x = xStart;
        this.y = yStart;
    }
}