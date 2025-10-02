package com.jacandre;

import com.jacandre.models.*;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class Simulation {
    private final List<Agent> livingAgents;
    private final List<Food> activeFoodSources = new ArrayList<>();
    private final GridManager gridManager;
    private int tick;

    public Simulation() {
        this.livingAgents = new ArrayList<>();
        this.gridManager = new GridManager(Constants.GRID_SIZE);
        this.tick = 0;

        initialiseAgents();
        generateFoodSource();

        log.info("Simulation initialized with {} agents on a {}x{} grid.", livingAgents.size(), Constants.GRID_SIZE, Constants.GRID_SIZE);
    }

    private void initialiseAgents() {
        for (int i = 0; i < Constants.NUM_AGENTS; i++) {
            Point pos = gridManager.getNextAvailablePosition();
            if (pos == null) {
                log.warn("Ran out of space while placing agents. {}/{} agents placed.", livingAgents.size(), Constants.NUM_AGENTS);
                break;
            }

            Strategy strategy = Constants.RANDOM.nextBoolean() ? Strategy.HELPER : Strategy.SELFISH;
            Agent agent = new Agent(strategy);

            boolean placed = gridManager.placeEntity(agent, pos);
            if (placed) {
                livingAgents.add(agent);
            } else {
                log.warn("Failed to place agent at {}", pos);
            }
        }

        log.info("{} agents populated to grid.", livingAgents.size());
    }

    private void generateFoodSource() {
        if (activeFoodSources.size() >= Constants.MAX_FOOD_SOURCES) {
            log.debug("Maximum food sources reached. Skipping generation.");
            return;
        }

        Point pos = gridManager.getNextAvailablePosition();
        if (pos == null) {
            log.warn("No available position to place food.");
            return;
        }

        Food food = new Food();
        if (gridManager.placeEntity(food, pos)) {
            activeFoodSources.add(food);
            log.info("Food source generated at {}", pos);
        }
    }

    // Remove expired Food
    private void cleanUpFoodSources() {
        Iterator<Food> iterator = activeFoodSources.iterator();
        while (iterator.hasNext()) {
            Food food = iterator.next();
            food.ageOneTick();
            if (food.isExpired()) {
                Point pos = gridManager.getPositionOf(food);
                gridManager.removeEntity(food);
                gridManager.releasePosition(pos);
                iterator.remove();
                log.info("Food at {} expired and removed.", pos);
            }
        }
    }

    public void stepSimulation() {
        tick++;
        log.debug("Tick {} begins", tick);

        cleanUpFoodSources();
        Collections.shuffle(livingAgents, Constants.RANDOM);

        List<Agent> agentsToRemove = new ArrayList<>();

        for (Agent agent : new ArrayList<>(livingAgents)) {
            Point pos = gridManager.getPositionOf(agent);
            if (pos == null) {
                log.warn("Agent {} has no position and will be removed.", agent.getId());
                agentsToRemove.add(agent);
                continue;
            }

            act(agent);
            maybeReproduce(agent);
            applyCostOfLiving(agent);

            if (agent.getEnergy() <= 0) {
                agentsToRemove.add(agent);
            }
        }

        for (Agent agent : agentsToRemove) {
            Point pos = gridManager.getPositionOf(agent);
            if (pos != null) {
                gridManager.removeEntity(agent);
                gridManager.releasePosition(pos);
            }
        }
        livingAgents.removeAll(agentsToRemove);

        maybeGenerateNewFood();
        log.info("Tick {} complete. {} agents remain.", tick, livingAgents.size());
    }

    private void moveAgent(Agent agent) {
        Point currentPos = gridManager.getPositionOf(agent);
        if (currentPos == null) {
            return;
        }

        List<Point> emptyNeighbours = gridManager.getEmptyNeighbours(currentPos, 1);
        if (emptyNeighbours.isEmpty()) {
            return;
        }

        Point target = emptyNeighbours.get(Constants.RANDOM.nextInt(emptyNeighbours.size()));
        boolean moved = gridManager.moveEntity(agent, target);

        if (moved) {
            agent.decreaseEnergy(Constants.COST_OF_LIVING);
        } else {
            log.debug("Agent {} failed to move to {}", agent.getId(), target);
        }
    }

    private void act(Agent agent) {
        Point pos = gridManager.getPositionOf(agent);
        List<Point> neighbours = gridManager.getNeighbourPositions(pos, 1);

        // 1. Prioritise food
        for (Point p : neighbours) {
            GridEntity entity = gridManager.getEntityAt(p);
            if (entity instanceof Food) {
                boolean moved = gridManager.moveEntity(agent, p);
                if (moved) {
                    agent.decreaseEnergy(Constants.COST_OF_LIVING);
                    consumeFoodIfPresent(agent); // assumes agent is now at food location
                }
                return;
            }
        }

        // 2. Assist if HELPER and no food found
        if (agent.isHelper()) {
            for (Point p : neighbours) {
                GridEntity entity = gridManager.getEntityAt(p);
                if (entity instanceof Agent other && other.getEnergy() < Constants.LOW_ENERGY_THRESHOLD) {
                    assistAgent(agent, other);
                    return;
                }
            }
        }

        // 3. Move to empty space
        List<Point> emptyNeighbours = gridManager.getEmptyNeighbours(pos, 1);
        if (!emptyNeighbours.isEmpty()) {
            Point target = emptyNeighbours.get(Constants.RANDOM.nextInt(emptyNeighbours.size()));
            boolean moved = gridManager.moveEntity(agent, target);
            if (moved) {
                agent.decreaseEnergy(Constants.COST_OF_LIVING);
            }
        }
    }


    private void consumeFoodIfPresent(Agent agent) {

    }

    private void assistAgent(Agent agent) {

    }

    private void maybeReproduce(Agent agent) {

    }

    private void applyCostOfLiving(Agent agent) {

    }

    public static void main( String[] args ) {

    }
}
