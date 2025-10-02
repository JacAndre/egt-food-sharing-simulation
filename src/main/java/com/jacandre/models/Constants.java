package com.jacandre.models;

import java.util.Random;

public class Constants {
    // Simulation Grid and Agent Count
    public static final int GRID_SIZE = 50;
    public static final int NUM_AGENTS = 1500; // Total number of agents (max GRID_SIZE * GRID_SIZE)
    public static final int MAX_FOOD_SOURCES = 5;

    // Core Game Parameters
    public static final double INITIAL_ENERGY = 100.0;
    public static final double COST_OF_LIVING = 0.5; // Includes movement cost
    public static final double FOOD_REWARD = 20.0;
    public static final int FOOD_LIFESPAN = 100;

    // Assist Mechanism Parameters
    public static final double ASSIST_COST = 2.0; // Cost incurred by Helper when offering assistance
    public static final double SHARE_RATE = 0.2; // Percentage of FOOD_REWARD the Helper demands
    public static final double LOW_ENERGY_THRESHOLD = 5.0; // Fitness level to trigger an assist

    // Evolutionary Dynamics Parameters
    public static final double BETA = 0.1; // Selection strength for the Fermi update rule (higher = stronger selection)

    // Utility for randomization (ensure consistent random numbers across all classes)
    public static final Random RANDOM = new Random();
}
