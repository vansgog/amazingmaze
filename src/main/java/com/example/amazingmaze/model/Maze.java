package com.example.amazingmaze.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Entity
@Getter
@Setter
@NoArgsConstructor(force = true)
public class Maze {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private int size, rows, cols;
    @Transient
    private final int[][] maze;
    @Transient
    private boolean[][] visited;
    @Transient
    private CopyOnWriteArrayList<TimePortal> portals = new CopyOnWriteArrayList<>();
    @Transient
    private final Stack<int[]> stack = new Stack<>();

    public static final int OPEN_CELL = 0;
    public static final int WALL = 1;
    public static final int EXIT = 2;

    public Maze(int size) {
        this.rows = size * 2 + 1;
        this.cols = size * 2 + 1;
        this.maze = new int[rows][cols];
        this.visited = new boolean[rows][cols];
        this.portals = new CopyOnWriteArrayList<>();
    }
}