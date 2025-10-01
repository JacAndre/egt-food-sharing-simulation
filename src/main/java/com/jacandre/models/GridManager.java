package com.jacandre.models;

import java.awt.Point;
import java.util.*;

public class GridManager {
    private final GridEntity[][] grid;
    private final Map<GridEntity, Point> entityPositions;
    private final List<Point> unoccupiedPositions;
    private final int gridSize;

    public GridManager(int gridSize) {
        this.gridSize = gridSize;
        this.grid = new GridEntity[gridSize][gridSize];
        this.entityPositions = new HashMap<>();
        this.unoccupiedPositions = initialiseUnoccupiedPositions();
    }

    private List<Point> initialiseUnoccupiedPositions() {
        List<Point> unoccupiedPositions = new ArrayList<>();

        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                unoccupiedPositions.add(new Point(x, y));
            }
        }

        Collections.shuffle(unoccupiedPositions, Constants.RANDOM);
        return unoccupiedPositions;
    }

    public Point getNextAvailablePosition() {
        if (unoccupiedPositions.isEmpty()) return null;
        return unoccupiedPositions.removeFirst();
    }

    public boolean placeEntity(GridEntity entity, Point position) {
        Point wrapped = wrap(position);
        // If position is occupied, abort.
        if (grid[wrapped.x][wrapped.y] != null) {
            return false;
        }

        grid[wrapped.x][wrapped.y] = entity;
        entityPositions.put(entity, wrapped);

        return true;
    }

    public boolean moveEntity(GridEntity entity, Point newPosition) {
        Point wrapped = wrap(newPosition);
        if (!entityPositions.containsKey(entity)) {
            return false;
        }
        if (grid[wrapped.x][wrapped.y] != null) {
            return false;
        }

        Point oldPos = entityPositions.get(entity);
        grid[oldPos.x][oldPos.y] = null;
        grid[wrapped.x][wrapped.y] = entity;
        entityPositions.put(entity, wrapped);

        return true;
    }

    public void removeEntity(GridEntity entity) {
        Point position = entityPositions.remove(entity);
        if (position != null) {
            grid[position.x][position.y] = null;
        }
    }

    public void releasePosition(Point p) {
        Point wrapped = wrap(p);
        if (!isOccupied(wrapped)) {
            unoccupiedPositions.add(wrapped);
        }
    }

    public GridEntity getEntityAt(Point position) {
        Point wrapped = wrap(position);
        return grid[wrapped.x][wrapped.y];
    }

    public Point getPosition(GridEntity entity) {
        return entityPositions.get(entity);
    }

    public boolean isOccupied(Point position) {
        Point wrapped = wrap(position);
        return grid[wrapped.x][wrapped.y] != null;
    }

    public List<Point> getEmptyNeighbors(Point center, int radius) {
        List<Point> empty = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                if (dx == 0 && dy == 0) continue;
                Point neighbor = wrap(new Point(center.x + dx, center.y + dy));
                if (!isOccupied(neighbor)) empty.add(neighbor);
            }
        }
        return empty;
    }

    public int availableCount() {
        return unoccupiedPositions.size();
    }

    public void reshuffleAvailablePositions() {
        Collections.shuffle(unoccupiedPositions, Constants.RANDOM);
    }

    private Point wrap(Point p) {
        int x = (p.x % gridSize + gridSize) % gridSize;
        int y = (p.y % gridSize + gridSize) % gridSize;
        return new Point(x, y);
    }
}
