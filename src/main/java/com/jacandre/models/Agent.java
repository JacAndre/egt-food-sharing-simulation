package com.jacandre.models;

import lombok.Getter;
import lombok.Setter;

// Represents a single agent (strategy, energy, location on the grid) in the spatial game.

// TODO: Strategy Composition for Agent Behaviour (Post-MVP)
//
// Goal:
// Refactor agent decision logic to use runtime-adaptable strategies via composition.
// This will allow agents to switch roles dynamically (e.g. Forager, Helper, Explorer)
// and encapsulate behaviour cleanly within strategy classes.
//
// Design Plan:
// - Introduce AgentStrategy interface with method: void execute(Agent agent, GridManager grid, SimulationContext context)
// - Refactor Agent class to hold a strategy field:
//     private AgentStrategy strategy;
//     public void act(GridManager grid, SimulationContext context) {
//         strategy.execute(this, grid, context);
//     }
// - Implement role-specific strategies:
//     - ForagerStrategy: prioritises food acquisition
//     - HelperStrategy: assists nearby low-energy agents when no food is present
//     - ExplorerStrategy: seeks empty space or unexplored areas
// - Allow strategy mutation via setStrategy(AgentStrategy newStrategy)
// - Consider SimulationContext wrapper to pass tick, nearby entities, global constants, etc.
//
// Benefits:
// - Clean separation of concerns
// - Easy to extend with new behaviours
// - Supports future evolution, role mutation, and hybrid strategies
//
// Status:
// Deferred until MVP is complete and core simulation loop is stable.

@Getter
@Setter
public class Agent implements GridEntity {
    private final String id;
    private Strategy strategy;
    private double energy;

    public Agent(Strategy initialStrategy) {
        this.id = java.util.UUID.randomUUID().toString();
        this.strategy = initialStrategy;
        this.energy = Constants.INITIAL_ENERGY;
    }

    // GETTERS

    public boolean isHelper() {
        return this.strategy == Strategy.HELPER;
    }

    public boolean isSelfish() {
        return this.strategy == Strategy.SELFISH;
    }

    // MODIFIERS
    public void decreaseEnergy(double cost) {
        this.energy -= cost;
    }

    public void increaseEnergy(double reward) {
        this.energy += reward;
    }
}
