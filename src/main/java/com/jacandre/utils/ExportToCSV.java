package com.jacandre.utils;

import com.jacandre.models.SimulationMetrics;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Slf4j
public class ExportToCSV {
    public static void exportMetricsToCSV(String filename, List<SimulationMetrics> metricsHistory) {
        try (PrintWriter writer = new PrintWriter(filename)) {
            writer.println("Tick Number,Average Energy,Total Births,Total Deaths,HELPER Agents,SELFISH Agents,HELPER Births,SELFISH Births,Energy Snapshot");

            for (SimulationMetrics metrics : metricsHistory) {
                writer.println(metrics.toCSVRow());
            }

            System.out.println("Metrics exported to " + filename);
        } catch (IOException e) {
            log.error("Export to CSV failed: {}", (Object) e.getStackTrace());
        }
    }

}
