package com.jacandre;

public class Main {
    public static void main(String[] args) {
        long startTime = System.nanoTime();
        Simulation sim = new Simulation();
        int maxTicks = 1000;

        for (int i = 0; i < maxTicks; i++) {
            sim.stepSimulation();

            if (sim.getLivingAgents().isEmpty()) {
                System.out.println("All agents died at tick " + sim.getTick());
                break;
            }
        }

        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;

        System.out.println("Simulation ended at tick " + sim.getTick());
        System.out.println("Final agent count: " + sim.getLivingAgents().size());
        System.out.println("Total execution time: " + durationMs + " ms");
    }
}
