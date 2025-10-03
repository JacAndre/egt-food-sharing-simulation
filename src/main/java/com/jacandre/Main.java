package com.jacandre;

import com.jacandre.export.SimulationExporter;
import com.jacandre.models.Constants;
import com.jacandre.utils.ExportToCSV;

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

        Simulation simulation = new Simulation();

        for (int i = 0; i < maxTicks; i++) {
            simulation.stepSimulation();
            if (simulation.getLivingAgents().isEmpty()) {
                System.out.println("All agents died at tick " + simulation.getTick());
                break;
            }
        }

        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;

        System.out.println("Simulation ended at tick " + simulation.getTick());
        System.out.println("Final agent count: " + simulation.getLivingAgents().size());
        System.out.println("Total execution time: " + durationMs + " ms");

        ExportToCSV.exportMetricsToCSV("simulation_metrics.csv", simulation.getMetricsHistory());
        SimulationExporter.exportGridSnapshots("simulation_grid_snapshots.csv", simulation.getTimeline().getSnapshots(), simulation.getGridManager());
    }
}
