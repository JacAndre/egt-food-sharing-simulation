package com.jacandre.utils;

import com.jacandre.models.SimulationMetrics;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class ExportToCSV {
    public static void exportMetricsToCSV(String filename, List<SimulationMetrics> metricsHistory) {
        try (PrintWriter writer = new PrintWriter(new File(filename))) {
            writer.println("Tick Number,Average Energy,Total Births,Total Deaths,HELPER Agents,SELFISH Agents,HELPER Births,SELFISH Births");

            for (SimulationMetrics metrics : metricsHistory) {
                writer.println(metrics.toCSVRow());
            }

            System.out.println("Metrics exported to " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
