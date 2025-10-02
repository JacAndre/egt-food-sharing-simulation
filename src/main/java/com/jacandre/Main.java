package com.jacandre;

import com.jacandre.models.Constants;

public class Main {
    public static void main(String[] args) {
        int maxTicks = 1000;
        int numAgents = Constants.NUM_AGENTS;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--ticks":
                    maxTicks = Integer.parseInt(args[++i]);
                    break;
                case "--agents":
                    numAgents = Integer.parseInt(args[++i]);
                    break;
                default:
                    System.out.println("Unknown argument: " + args[i]);
            }
        }

        Constants.NUM_AGENTS = numAgents; // override default
        long startTime = System.nanoTime();

        Simulation sim = new Simulation();

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
