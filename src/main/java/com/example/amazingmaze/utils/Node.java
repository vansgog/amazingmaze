package com.example.amazingmaze.utils;

import lombok.Getter;

@Getter
public class Node implements Comparable<Node> {
    private final int x, y;
    private final Node parent;
    private final int cost;
    private final int heuristic;
    private final int totalCost;

    public Node(int x, int y, Node parent, int cost, int heuristic) {
        this.x = x;
        this.y = y;
        this.parent = parent;
        this.cost = cost;
        this.heuristic = heuristic;
        this.totalCost = cost + heuristic;
    }

    public static int getHeuristic(int x, int y, int endX, int endY) {
        return Math.abs(x - endX) + Math.abs(y - endY);
    }

    @Override
    public int compareTo(Node other) {
        return Integer.compare(this.totalCost, other.totalCost);
    }
}