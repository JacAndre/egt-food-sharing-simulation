package com.jacandre.models;

import lombok.Getter;
import lombok.Setter;

// Represents a single agent (strategy, energy, location on the grid) in the spatial game.

@Getter
@Setter
public class Agent implements GridEntity {
    private final String id;
    private Strategy strategy;
    private double fitness;

    public Agent(Strategy initialStrategy) {
        this.id = java.util.UUID.randomUUID().toString();
        this.strategy = initialStrategy;
        this.fitness = Constants.INITIAL_ENERGY;
    }

    // MODIFIERS
    public void decreaseFitness(double cost) {
        this.fitness -= cost;
    }

    public void increaseFitness(double reward) {
        this.fitness += reward;
    }
}
