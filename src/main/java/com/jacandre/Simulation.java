package com.jacandre;

import com.jacandre.models.*;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class Simulation {
    private final List<Agent> livingAgents;
    private final GridManager gridManager;
    private Point foodSource;
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


    }

    public static void main( String[] args ) {

    }
}
