package com.jacandre;

import com.jacandre.models.*;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.util.ArrayList;
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



    public static void main( String[] args ) {

    }
}
