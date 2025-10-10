package com.jacandre.core;

import com.jacandre.models.Agent;
import com.jacandre.models.Food;
import com.jacandre.models.GridEntity;
import lombok.extern.slf4j.Slf4j;
import net.jcip.annotations.ThreadSafe;

import java.awt.Point;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Slf4j
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

    public boolean tryConsumeFood(Point foodPos, Agent contender) {
        Point wrapped = wrap(foodPos);
        ReentrantLock lock = cellLocks[wrapped.x][wrapped.y];
        lock.lock();

        try {
            GridEntity entity = grid[wrapped.x][wrapped.y];
            if (!(entity instanceof Food)) return false;

            // Scan for adjacent agents
            List<GridEntity> neighbours = getOccupiedNeighbours(wrapped, 1);
            List<Agent> contenders = neighbours.stream()
                    .filter(e -> e instanceof Agent)
                    .map(e -> (Agent) e)
                    .filter(a -> getPositionOf(a).distance(wrapped) <= 1.0)
                    .toList();

            Agent strongest = contenders.stream()
                    .max(Comparator.comparingDouble(Agent::getEnergy))
                    .orElse(null);

            if (strongest != null && strongest.equals(contender)) {
                grid[wrapped.x][wrapped.y] = null;
                entityPositions.remove(entity);
                releasePosition(wrapped);
                contender.increaseEnergy(Constants.FOOD_REWARD);
                log.info("Agent {} won contested food at {} with energy {}", contender.getId(), foodPos, contender.getEnergy());
                return true;
            }

            return false;
        } finally {
            lock.unlock();
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

                neighbours.add(wrap(new Point(centre.x + dx, centre.y + dy)));
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

    public Map<Point, GridEntity> getNeighbourEntities(Point centre, int radius) {
        return getNeighbourPositions(centre, radius).stream()
                .collect(Collectors.toMap(p -> p, this::getEntityAt));
    }

    public List<Point> getEntitiesOfType(Point centre, int radius, Class<? extends GridEntity> type) {
        return getNeighbourPositions(centre, radius).stream()
                .filter(p -> type.isInstance(getEntityAt(p)))
                .collect(Collectors.toList());
    }

    public Optional<Point> findNearest(Point from, List<Point> targets) {
        return targets.stream()
                .min(Comparator.comparingDouble(from::distanceSq));
    }

    public boolean consumeEntity(Point p, Agent consumer, double reward) {
        GridEntity entity = getEntityAt(p);
        if (entity == null) return false;
        removeEntity(entity);
        consumer.increaseEnergy(reward);
        return true;
    }

    public Map<Point, GridEntity> getOccupiedEntities() {
        return getEntitiesOfType(GridEntity.class); // all non-null entities
    }

    public Map<Point, GridEntity> getAgentEntities() {
        return getEntitiesOfType(Agent.class);
    }

    public Map<Point, GridEntity> getFoodEntities() {
        return getEntitiesOfType(Food.class);
    }

    public Map<Point, GridEntity> getEntitiesOfType(Class<? extends GridEntity> type) {
        Map<Point, GridEntity> result = new HashMap<>();
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                GridEntity entity = grid[x][y];
                if (type.isInstance(entity)) {
                    result.put(new Point(x, y), entity);
                }
            }
        }
        return result;
    }


    public Point stepToward(Point from, Point to) {
        int dx = Integer.compare(to.x, from.x);
        int dy = Integer.compare(to.y, from.y);
        return wrap(new Point(from.x + dx, from.y + dy));
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
