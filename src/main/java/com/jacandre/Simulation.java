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

    private List<Point> initialisePositions() {
        List<Point> availablePositions = new ArrayList<>();
        for (int x = 0; x < Constants.GRID_SIZE; x++) {
            for (int y = 0; y < Constants.GRID_SIZE; y++) {
                availablePositions.add(new Point(x, y));
            }
        }

        Collections.shuffle(availablePositions, Constants.RANDOM);
        return availablePositions;
    }

    private void initialiseAgents() {
        // All possible positions
        List<Point> availablePositions = new ArrayList<>();
        for(int i = 0; i < Constants.GRID_SIZE; i++) {
            for(int j = 0; j < Constants.GRID_SIZE; j++) {
                availablePositions.add(new Point(j, i));
            }
        }

        Collections.shuffle(availablePositions);

        for (int i = 0; i < Constants.NUM_AGENTS && i < availablePositions.size(); i++) {
            Point pos = availablePositions.get(i);
            Strategy strategy = Constants.RANDOM.nextBoolean() ? Strategy.HELPER : Strategy.SELFISH;
            Agent agent = new Agent(strategy);
            gridManager.placeEntity(agent, pos);
            livingAgents.add(agent);
        }

        log.info("{} Agents populated to Grid", livingAgents.size());
    }

    private void generateFoodSource() {


    }

    public static void main( String[] args ) {

    }
}
