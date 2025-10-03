package com.jacandre;

import com.jacandre.core.Constants;
import com.jacandre.core.GridManager;
import com.jacandre.core.Simulation;
import com.jacandre.models.*;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class SimulationTest {

    @Test
    void agentConsumesFoodWhenAdjacent() {
        GridManager grid = new GridManager(5);
        Agent agent = new Agent(Strategy.SELFISH);
        Food food = new Food();

        Point agentPos = new Point(2, 2);
        Point foodPos = new Point(2, 3);

        grid.placeEntity(agent, agentPos);
        grid.placeEntity(food, foodPos);

        Random deterministic = new Random(42);
        Simulation sim = new Simulation(grid, List.of(agent), List.of(), deterministic);
        sim.stepSimulation();

        assertTrue(agent.getEnergy() > Constants.INITIAL_ENERGY, "Agent should gain energy from food");
        assertInstanceOf(Agent.class, grid.getEntityAt(foodPos), "Food should be consumed and removed and replaced with Agent");
    }

    @Test
    void helperAssistsLowEnergyAgent() {
        GridManager grid = new GridManager(5);
        Agent helper = new Agent(Strategy.HELPER);
        Agent recipient = new Agent(Strategy.SELFISH);
        recipient.decreaseEnergy(Constants.INITIAL_ENERGY - Constants.LOW_ENERGY_THRESHOLD + 1);

        Point helperPos = new Point(2, 2);
        Point recipientPos = new Point(2, 3);

        grid.placeEntity(helper, helperPos);
        grid.placeEntity(recipient, recipientPos);

        Random deterministic = new Random(42);
        Simulation sim = new Simulation(grid, List.of(helper, recipient), List.of(), deterministic);
        sim.stepSimulation();

        assertTrue(recipient.getEnergy() > Constants.LOW_ENERGY_THRESHOLD);
        assertTrue(helper.getEnergy() < Constants.INITIAL_ENERGY);
    }

    @Test
    void agentMovesToEmptySpaceWhenNoFoodOrAgentsNearby() {
        GridManager grid = new GridManager(5);
        Agent agent = new Agent(Strategy.SELFISH);
        Point agentPos = new Point(2, 2);
        grid.placeEntity(agent, agentPos);

        Random deterministic = new Random(42);
        Simulation sim = new Simulation(grid, List.of(agent), List.of(), deterministic);
        sim.stepSimulation();

        Point newPos = grid.getPositionOf(agent);
        assertNotEquals(agentPos, newPos, "Agent should have moved to a new position");
    }

    @Test
    void agentDoesNotMoveWhenSurrounded() {
        GridManager grid = new GridManager(3);
        Agent centre = new Agent(Strategy.SELFISH);
        Point centrePos = new Point(1, 1);
        grid.placeEntity(centre, centrePos);

        // Surround the agent
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                Agent blocker = new Agent(Strategy.SELFISH);
                grid.placeEntity(blocker, new Point(1 + dx, 1 + dy));
            }
        }

        Random deterministic = new Random(42);
        Simulation sim = new Simulation(grid, List.of(centre), List.of(), deterministic);
        sim.stepSimulation();

        Point newPos = grid.getPositionOf(centre);
        assertEquals(centrePos, newPos, "Agent should not move when surrounded");
    }

    @Test
    void agentDiesWhenEnergyDepletes() {
        GridManager grid = new GridManager(5);
        Agent agent = new Agent(Strategy.SELFISH);
        agent.decreaseEnergy(Constants.INITIAL_ENERGY); // Set to 0

        Point pos = new Point(2, 2);
        grid.placeEntity(agent, pos);

        Random deterministic = new Random(42);
        Simulation sim = new Simulation(grid, List.of(agent), List.of(), deterministic);
        sim.stepSimulation();

        assertNull(grid.getEntityAt(pos), "Agent should be removed from grid");
    }

    @Test
    void foodExpiresAfterMaxAge() {
        GridManager grid = new GridManager(5);
        Food food = new Food();
        Point pos = new Point(2, 2);
        grid.placeEntity(food, pos);

        Random deterministic = new Random(42);
        Simulation sim = new Simulation(grid, List.of(), List.of(food), deterministic);

        for (int i = 0; i < Constants.FOOD_LIFESPAN + 1; i++) {
            sim.stepSimulation();
        }

        assertNull(grid.getEntityAt(pos), "Food should be removed after expiration");
    }

    @Test
    void agentRespectsReproductionCooldown() {
        GridManager grid = new GridManager(5);
        Agent parent = new Agent(Strategy.SELFISH);
        parent.setEnergy(Constants.INITIAL_ENERGY);

        Point parentPos = new Point(2, 2);
        grid.placeEntity(parent, parentPos);

        Simulation sim = new Simulation(grid, List.of(parent), List.of(), new Random(42));

        // Run ticks below cooldown threshold
        for (int i = 0; i < Constants.REPRODUCTION_COOLDOWN - 1; i++) {
            sim.stepSimulation();
        }

        assertEquals(1, sim.getLivingAgents().size(), "Agent should not reproduce before cooldown");

        // Run one more tick to reach cooldown
        sim.stepSimulation();

        assertEquals(2, sim.getLivingAgents().size(), "Agent should reproduce after cooldown");
    }

}
