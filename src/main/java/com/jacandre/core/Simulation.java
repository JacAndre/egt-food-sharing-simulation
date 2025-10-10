package com.jacandre.core;

import com.jacandre.models.*;
import com.jacandre.strategy.AgentStrategy;
import com.jacandre.strategy.HelperStrategy;
import com.jacandre.strategy.SelfishStrategy;
import com.jacandre.timeline.SimulationMetrics;
import com.jacandre.timeline.TickHistory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class Simulation {
    private final List<Agent> livingAgents;
    private final List<Food> activeFoodSources;
    private final GridManager gridManager;
    private final TickHistory timeline = new TickHistory();
    private int tick;

    private final AgentStrategy helperStrategy = new HelperStrategy();
    private final AgentStrategy selfishStrategy = new SelfishStrategy();

    private final List<SimulationMetrics> metricsHistory = new ArrayList<>();

    private int totalDeaths = 0;
    private int helperBirths = 0;
    private int selfishBirths = 0;
    private int helperCount = 0;
    private int selfishCount = 0;
    private double cumulativeEnergy = 0.0;
    private double avgEnergy = 0.0;

    public Simulation() {
        this(new GridManager(Constants.GRID_SIZE), new ArrayList<>(), new ArrayList<>(), Constants.RANDOM);
        initialiseAgents();
        generateFoodSource();

        log.info("Simulation initialised with {} agents on a {}x{} grid.",
                livingAgents.size(), Constants.GRID_SIZE, Constants.GRID_SIZE);
    }

    // For testing
    public Simulation(GridManager gridManager, List<Agent> agents, List<Food> foodSources, Random random) {
        this.gridManager = gridManager;
        this.livingAgents = new ArrayList<>(agents);
        this.activeFoodSources = new ArrayList<>(foodSources);
        this.tick = 0;
        Collections.shuffle(this.livingAgents, random);
    }

    private void initialiseAgents() {
        for (int i = 0; i < Constants.NUM_AGENTS; i++) {
            Point pos = gridManager.getNextAvailablePosition();
            if (pos == null) {
                log.warn("Ran out of space while placing agents. {}/{} agents placed.", livingAgents.size(), Constants.NUM_AGENTS);
                break;
            }

            AgentStrategy strategy = Constants.RANDOM.nextBoolean() ? helperStrategy : selfishStrategy;
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
        SimulationContext context = new SimulationContext(tick, Constants.RANDOM);

        tick++;
        log.debug("Tick {} begins", tick);

        cleanUpFoodSources();

        List<Agent> agentsToRemove = new ArrayList<>();

        for (Agent agent : new ArrayList<>(livingAgents)) {
            Point pos = gridManager.getPositionOf(agent);
            if (pos == null) {
                log.warn("Agent {} has no position and will be removed.", agent.getId());
                agentsToRemove.add(agent);
                continue;
            }

            act(agent, context);
            maybeReproduce(agent, context);
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
                ++totalDeaths;
            }
        }
        livingAgents.removeAll(agentsToRemove);

        maybeGenerateNewFood(context);

        recordSnapshot(tick);

        helperCount = 0;
        selfishCount = 0;

        for (Agent agent : livingAgents) {
            if (agent.isHelper()) {
                helperCount++;
            } else if (agent.isSelfish()) {
                selfishCount++;
            }
            cumulativeEnergy += agent.getEnergy();
        }

        avgEnergy = livingAgents.isEmpty() ? 0.0 : cumulativeEnergy / livingAgents.size();

        log.info("Tick {} complete. {} agents remain.", tick, livingAgents.size());

        List<Double> energySnapshot = livingAgents.stream()
                .map(Agent::getEnergy)
                .collect(Collectors.toList());

        SimulationMetrics metrics = new SimulationMetrics();
        metrics.setTick(tick);
        metrics.setAvgEnergy(avgEnergy);
        metrics.setTotalDeaths(totalDeaths);
        metrics.setHelperCount(helperCount);
        metrics.setSelfishCount(selfishCount);
        metrics.setHelperBirths(helperBirths);
        metrics.setSelfishBirths(selfishBirths);
        metrics.setEnergySnapshot(energySnapshot);

        metrics.logMetrics();
        metricsHistory.add(metrics);

        cumulativeEnergy = 0.0;

    }

    // TODO: Add strength-based arbitration for contested food consumption
    //
    // Goal:
    // When multiple agents attempt to consume the same food source in the same tick,
    // resolve the conflict by allowing only the strongest (highest energy) agent to succeed.
    //
    // Design Plan:
    // - Track all agents targeting the same food location
    // - Compare their energy levels before executing consumeFoodAt()
    // - Allow only the strongest agent to proceed; others must fallback
    // - Consider tie-breakers (e.g. random, agent ID) for equal energy
    //
    // Benefits:
    // - Adds realism and strategic tension to food competition
    // - Prevents simultaneous consumption bugs
    // - Encourages agents to maintain high energy for priority access
    //
    // Status:
    // Deferred until MVP is stable and single-agent decision flow is complete.

    private void act(Agent agent, SimulationContext context) {
        agent.act(gridManager, context);
    }

    private void maybeReproduce(Agent agent, SimulationContext context) {
        if (agent.getEnergy() < Constants.REPRODUCTION_THRESHOLD) return;
        if (tick - agent.getLastReproducedTick() < Constants.REPRODUCTION_COOLDOWN) return;

        Point parentPos = gridManager.getPositionOf(agent);
        List<Point> emptyNeighbours = gridManager.getEmptyNeighbours(parentPos, 1);
        if (emptyNeighbours.isEmpty()) return;

        Point childPos = emptyNeighbours.get(context.random().nextInt(emptyNeighbours.size()));
        Agent child = new Agent(agent.getStrategy());

        if (gridManager.placeEntity(child, childPos)) {
            double splitEnergy = agent.getEnergy() / 2.0;
            agent.setEnergy(splitEnergy);
            child.setEnergy(splitEnergy);
            livingAgents.add(child);

            agent.setLastReproducedTick(tick); // update cooldown
            if (child.isHelper()) {
                helperBirths++;
            } else if (child.isSelfish()) {
                selfishBirths++;
            }

            log.info("Agent {} reproduced at tick {}. Child agent {} created at {} with {} energy.",
                    agent.getId(), tick, child.getId(), childPos, splitEnergy);
        }
    }

    private void maybeGenerateNewFood(SimulationContext context) {
        if (activeFoodSources.size() >= Constants.MAX_FOOD_SOURCES) {
            return;
        }
        if (context.random().nextDouble() < Constants.FOOD_SPAWN_PROBABILITY) {
            generateFoodSource();
        }
    }

    private void applyCostOfLiving(Agent agent) {
        agent.decreaseEnergy(Constants.COST_OF_LIVING);
    }

    public void recordSnapshot(int tick) {
        Map<Point, GridEntity> snapshotState = new HashMap<>();
        for (Point p : gridManager.getOccupiedPositions()) {
            GridEntity entity = gridManager.getEntityAt(p);
            snapshotState.put(new Point(p), entity); // copy position
        }

        timeline.addSnapshot(new TickSnapshot(tick, snapshotState));
    }
}
