package com.jacandre.models;

import net.jcip.annotations.ThreadSafe;

import java.awt.Point;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@ThreadSafe
public class GridManager {
    private final GridEntity[][] grid;
    private final ConcurrentHashMap<GridEntity, Point> entityPositions;
    private final ConcurrentLinkedQueue<Point> unoccupiedPositions;
    private final ReentrantLock[][] cellLocks;
    private final int gridSize;

    public GridManager(int gridSize) {
        this.gridSize = gridSize;
        this.grid = new GridEntity[gridSize][gridSize];
        this.entityPositions = new ConcurrentHashMap<>();
        this.unoccupiedPositions = initialiseUnoccupiedPositions();
        this.cellLocks = new ReentrantLock[gridSize][gridSize];

        initialiseCellLocks();
    }

    private ConcurrentLinkedQueue<Point> initialiseUnoccupiedPositions() {
        List<Point> unoccupiedPositions = new ArrayList<>();
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                unoccupiedPositions.add(new Point(x, y));
            }
        }
        Collections.shuffle(unoccupiedPositions, Constants.RANDOM);
        return new ConcurrentLinkedQueue<>(unoccupiedPositions);
    }

    private void initialiseCellLocks() {
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                this.cellLocks[x][y] = new ReentrantLock();
            }
        }
    }

    public Point getNextAvailablePosition() {
        return unoccupiedPositions.poll(); // returns null if empty
    }

    public boolean placeEntity(GridEntity entity, Point position) {
        Point wrapped = wrap(position);
        ReentrantLock lock = cellLocks[wrapped.x][wrapped.y];

        lock.lock();
        try {
            if (grid[wrapped.x][wrapped.y] != null) {
                return false;
            }

            grid[wrapped.x][wrapped.y] = entity;
            entityPositions.put(entity, wrapped);
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Thread-safe movement using fine-grained cell locks.
     * Must acquire locks on both source and destination cells before mutation.
     */
    public boolean moveEntity(GridEntity entity, Point newPosition) {
        Point oldPos = entityPositions.get(entity);
        if (oldPos == null) {
            return false;
        }

        Point wrappedNew = wrap(newPosition);
        ReentrantLock lockA = cellLocks[oldPos.x][oldPos.y];
        ReentrantLock lockB = cellLocks[wrappedNew.x][wrappedNew.y];

        // Consistent lock ordering to prevent deadlock
        if (lockA == lockB) {
            lockA.lock();
        } else if (System.identityHashCode(lockA) < System.identityHashCode(lockB)) {
            lockA.lock();
            lockB.lock();
        } else {
            lockB.lock();
            lockA.lock();
        }

        try {
            if (grid[wrappedNew.x][wrappedNew.y] != null) return false;

            grid[oldPos.x][oldPos.y] = null;
            grid[wrappedNew.x][wrappedNew.y] = entity;
            entityPositions.put(entity, wrappedNew);
            releasePosition(oldPos);
            return true;
        } finally {
            if (lockA != lockB) lockB.unlock();
            lockA.unlock();
        }
    }

    public void removeEntity(GridEntity entity) {
        Point position = entityPositions.get(entity);
        if (position == null) {
            return;
        }

        Point wrapped = wrap(position);
        ReentrantLock lock = cellLocks[wrapped.x][wrapped.y];

        lock.lock();
        try {
            grid[wrapped.x][wrapped.y] = null;
            entityPositions.remove(entity);
            releasePosition(wrapped);
        } finally {
            lock.unlock();
        }
    }

    public void releasePosition(Point p) {
        Point wrapped = wrap(p);
        if (!isOccupied(wrapped)) {
            unoccupiedPositions.offer(wrapped);
        }
    }

    public GridEntity getEntityAt(Point position) {
        Point wrapped = wrap(position);
        return grid[wrapped.x][wrapped.y];
    }

    public Point getPositionOf(GridEntity entity) {
        return entityPositions.get(entity);
    }

    public boolean isOccupied(Point position) {
        Point wrapped = wrap(position);
        return grid[wrapped.x][wrapped.y] != null;
    }

    public List<Point> getNeighbourPositions(Point centre, int radius) {
        List<Point> neighbours = new ArrayList<>();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                if (dx == 0 && dy == 0) continue;

                Point neighbour = new Point(centre.x + dx, centre.y + dy);
                neighbours.add(wrap(neighbour));
            }
        }

        return neighbours;
    }

    public List<Point> getEmptyNeighbours(Point centre, int radius) {
        return getNeighbourPositions(centre, radius).stream()
                .filter(p -> !isOccupied(p))
                .collect(Collectors.toList());
    }

    public List<GridEntity> getOccupiedNeighbours(Point centre, int radius) {
        return getNeighbourPositions(centre, radius).stream()
                .map(this::getEntityAt)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    public List<Point> getOccupiedPositions() {
        List<Point> occupied = new ArrayList<>();
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                if (grid[x][y] != null) occupied.add(new Point(x, y));
            }
        }
        return occupied;
    }

    public int availableCount() {
        return unoccupiedPositions.size();
    }

    public void reshuffleAvailablePositions() {
        List<Point> temp = new ArrayList<>(unoccupiedPositions);
        Collections.shuffle(temp, Constants.RANDOM);
        unoccupiedPositions.clear();
        unoccupiedPositions.addAll(temp);
    }

    private Point wrap(Point p) {
        int x = (p.x % gridSize + gridSize) % gridSize;
        int y = (p.y % gridSize + gridSize) % gridSize;
        return new Point(x, y);
    }
}
