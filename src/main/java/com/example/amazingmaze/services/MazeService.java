package com.example.amazingmaze.services;

import com.example.amazingmaze.model.Maze;
import com.example.amazingmaze.model.TimePortal;
import com.example.amazingmaze.utils.Node;
import org.apache.commons.lang3.mutable.MutableInt;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class MazeService {
    private final Random random = new Random();
    private static final int[][] DIRS = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};

    public void initializeMaze(Maze maze) {
        for (int i = 0; i < maze.getRows(); i++) {
            for (int j = 0; j < maze.getCols(); j++) {
                maze.getMaze()[i][j] = Maze.WALL;
                maze.getVisited()[i][j] = false;
            }
        }
    }

    public void generateSomeMaze(Maze maze, int xStart, int yStart) {
        maze.getStack().push(new int[]{yStart, xStart});
        maze.getVisited()[yStart][xStart] = true;
        while (!maze.getStack().isEmpty()) {
            int[] current = maze.getStack().peek();
            int x = current[1];
            int y = current[0];
            List<int[]> directions = getAvailableDirections(maze, x, y, true);
            if (!directions.isEmpty()) {
                int[] direction = directions.get(random.nextInt(directions.size()));
                int nextX = direction[1];
                int nextY = direction[0];
                for (int i = Math.min(x, nextX); i <= Math.max(x, nextX); i++) {
                    for (int j = Math.min(y, nextY); j <= Math.max(y, nextY); j++) {
                        maze.getMaze()[j][i] = Maze.OPEN_CELL;
                        maze.getVisited()[j][i] = true;
                    }
                }
                maze.getStack().push(new int[]{nextY, nextX});
            } else maze.getStack().pop();
        }
    }

    public List<int[]> getAvailableDirections(Maze maze, int x, int y, boolean checkVisited) {
        List<int[]> directions = new ArrayList<>();
        if (checkVisited) {
            if (y >= 2 && !maze.getVisited()[y - 2][x]) directions.add(new int[]{y - 2, x});
            if (x >= 2 && !maze.getVisited()[y][x - 2]) directions.add(new int[]{y, x - 2});
            if (y < maze.getRows() - 2 && !maze.getVisited()[y + 2][x]) directions.add(new int[]{y + 2, x});
            if (x < maze.getCols() - 2 && !maze.getVisited()[y][x + 2]) directions.add(new int[]{y, x + 2});
        } else {
            if (y > 0 && maze.getMaze()[y-1][x] == Maze.OPEN_CELL) directions.add(new int[]{y-1, x});
            if (y < maze.getRows() - 1 && maze.getMaze()[y+1][x] == Maze.OPEN_CELL) directions.add(new int[]{y+1, x});
            if (x > 0 && maze.getMaze()[y][x-1] == Maze.OPEN_CELL) directions.add(new int[]{y, x-1});
            if (x < maze.getCols() - 1 && maze.getMaze()[y][x+1] == Maze.OPEN_CELL) directions.add(new int[]{y, x+1});
        }
        return directions;
    }

    private boolean isDeadEnd(Maze maze, int x, int y) {
        int wallsAround = 0;
        if (maze.getMaze()[y-1][x] == Maze.WALL) wallsAround++;
        if (maze.getMaze()[y+1][x] == Maze.WALL) wallsAround++;
        if (maze.getMaze()[y][x-1] == Maze.WALL) wallsAround++;
        if (maze.getMaze()[y][x+1] == Maze.WALL) wallsAround++;
        return wallsAround >= 3;
    }

    public void createTrapsAndBypasses(Maze maze) {
        for (int y = 2; y < maze.getRows() - 2; y += 2) {
            for (int x = 2; x < maze.getCols() - 2; x += 2) {
                if (maze.getMaze()[y][x] == Maze.OPEN_CELL &&
                    isDeadEnd(maze, x, y)) {
                    List<int[]> possibleDirections = new ArrayList<>();
                    for (int[] dir : DIRS) {
                        int newX = x + dir[0];
                        int newY = y + dir[1];
                        if (newX > 0 && newX < maze.getCols() - 1 &&
                            newY > 0 && newY < maze.getRows() - 1 &&
                            maze.getMaze()[newY][newX] == Maze.WALL) {
                            possibleDirections.add(dir);
                        }
                    }
                    if (!possibleDirections.isEmpty()) {
                        int[] chosenDirection = possibleDirections.get(random.nextInt(possibleDirections.size()));
                        int newX = x + chosenDirection[0];
                        int newY = y + chosenDirection[1];
                        maze.getMaze()[newY][newX] = Maze.OPEN_CELL;
                        maze.getVisited()[newY][newX] = true;
                        int oppositeX = x + chosenDirection[0] * 2;
                        int oppositeY = y + chosenDirection[1] * 2;
                        if (oppositeX >= 0 && oppositeX < maze.getCols() &&
                            oppositeY >= 0 && oppositeY < maze.getRows()) {
                            maze.getMaze()[oppositeY][oppositeX] = Maze.OPEN_CELL;
                            maze.getVisited()[oppositeY][oppositeX] = true;
                        }
                    }
                }
            }
        }
    }

    private void findFurthestExit(Maze maze,
                                 int x, int y,
                                 int currentLength,
                                 boolean[][] visited,
                                 Map<Integer, List<int[]>> furthestPoints,
                                 MutableInt maxDepth) {
        if (x < 0 || y < 0 ||
            x >= maze.getCols() || y >= maze.getRows() ||
            visited[y][x] ||
            maze.getMaze()[y][x] == Maze.WALL) return;
        visited[y][x] = true;
        if ((x == 1 || y == 1 ||
            x == maze.getCols() - 2 || y == maze.getRows() - 2) &&
            !(x == 1 && y == 1)){
            if (currentLength > maxDepth.getValue()) {
                maxDepth.setValue(currentLength);
                furthestPoints.clear();
            }
            furthestPoints
                    .computeIfAbsent(currentLength, k -> new ArrayList<>())
                    .add(new int[]{x, y});
        }
        if (x + 1 < maze.getCols() && !visited[y][x+1]) findFurthestExit(maze, x + 1, y,
                currentLength + 1,
                visited, furthestPoints, maxDepth);
        if (x - 1 >= 0 && !visited[y][x-1]) findFurthestExit(maze, x - 1, y,
                currentLength + 1,
                visited, furthestPoints, maxDepth);
        if (y + 1 < maze.getRows() && !visited[y+1][x]) findFurthestExit(maze, x, y + 1,
                currentLength + 1,
                visited, furthestPoints, maxDepth);
        if (y - 1 >= 0 && !visited[y-1][x]) findFurthestExit(maze, x, y - 1,
                currentLength + 1,
                visited, furthestPoints, maxDepth);
        visited[y][x] = false;
    }

    public void setExitBasedOnComplexity(Maze maze, int complexity) {
        boolean[][] visited = new boolean[maze.getRows()][maze.getCols()];
        Map<Integer, List<int[]>> furthestPoints = new TreeMap<>(Collections.reverseOrder());
        MutableInt maxDepth = new MutableInt(0);
        findFurthestExit(maze, 1, 1, 0, visited, furthestPoints, maxDepth);
        int targetDepth = calculateTargetDepth(furthestPoints, complexity);
        List<int[]> possibleExits = furthestPoints.get(targetDepth);
        if (possibleExits != null && !possibleExits.isEmpty()) {
            int[] exit = possibleExits.get(random.nextInt(possibleExits.size()));
            if (exit[1] == 1) {
                maze.getMaze()[exit[1]-1][exit[0]] = Maze.EXIT;
            } else if (exit[1] == maze.getRows() - 2) {
                maze.getMaze()[exit[1]+1][exit[0]] = Maze.EXIT;
            } else if (exit[0] == 1) {
                maze.getMaze()[exit[1]][exit[0]-1] = Maze.EXIT;
            } else if (exit[0] == maze.getCols() - 2) {
                maze.getMaze()[exit[1]][exit[0]+1] = Maze.EXIT;
            }
        }
    }

    private int calculateTargetDepth(Map<Integer, List<int[]>> furthestPoints, int complexity) {
        List<Integer> sortedDepths = new ArrayList<>(furthestPoints.keySet());
        Collections.sort(sortedDepths);
        if (sortedDepths.isEmpty()) throw new IllegalStateException("Нет доступных путей для выбора выхода");
        float complexityIndex = Math.round(((float) complexity / 10) * (sortedDepths.size() - 1));
        return sortedDepths.get((int) complexityIndex);
    }

    public void addPortal(Maze maze) {
        List<int[]> deadEnds = new ArrayList<>();
        for (int y = 1; y < maze.getRows() - 1; y += 2) {
            for (int x = 1; x < maze.getCols() - 1; x += 2) {
                if (!isPortalPresent(maze, x, y) &&
                    isDeadEnd(maze, x, y)) deadEnds.add(new int[]{x, y});
            }
        }
        Collections.shuffle(deadEnds);
        int[] location = deadEnds.get(random.nextInt(deadEnds.size()));
        maze.getPortals().add(new TimePortal(location[0], location[1], true));
    }

    public void removeRandomPortal(Maze maze) {
        if (!maze.getPortals().isEmpty()) maze.getPortals().remove(random.nextInt(maze.getPortals().size()));
    }

    public boolean isPortalPresent(Maze maze, int x, int y) {
        return maze.getPortals()
                   .stream()
                   .anyMatch(portal -> portal.getX() == x && portal.getY() == y && portal.isActive());
    }

    public CopyOnWriteArrayList<int[]> findPath(Maze maze, int startX, int startY, int endX, int endY) {
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        boolean[][] closedSet = new boolean[maze.getRows()][maze.getCols()];
        Node startNode = new Node(startX, startY, null, 0, Node.getHeuristic(startX, startY, endX, endY));
        openSet.add(startNode);
        while (!openSet.isEmpty()) {
            Node currentNode = openSet.poll();
            if (currentNode.getX() == endX && currentNode.getY() == endY) return reconstructPath(currentNode);
            closedSet[currentNode.getY()][currentNode.getX()] = true;
            for (int[] dir : DIRS) {
                int neighborX = currentNode.getX() + dir[0];
                int neighborY = currentNode.getY() + dir[1];
                if (isAccessible(maze, neighborX, neighborY, closedSet)) {
                    int neighborCost = currentNode.getCost() + 1;
                    Node neighborNode = new Node(neighborX, neighborY,
                                                 currentNode, neighborCost,
                                                 Node.getHeuristic(neighborX, neighborY, endX, endY));
                if (!openSet.contains(neighborNode)) openSet.add(neighborNode);
                }
            }
        }
        return new CopyOnWriteArrayList<>();
    }

    private CopyOnWriteArrayList<int[]> reconstructPath(Node node) {
        CopyOnWriteArrayList<int[]> path = new CopyOnWriteArrayList<>();
        while (node != null) {
            path.add(0, new int[]{node.getX(), node.getY()});
            node = node.getParent();
        }
        return path;
    }

    private boolean isAccessible(Maze maze, int x, int y, boolean[][] closedSet) {
        return x >= 0 && y >= 0 &&
               x < maze.getCols() && y < maze.getRows() &&
               maze.getMaze()[y][x] == Maze.OPEN_CELL && !closedSet[y][x];
    }
}