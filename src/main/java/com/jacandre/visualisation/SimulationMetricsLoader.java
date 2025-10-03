package com.jacandre.visualisation;

import com.jacandre.models.SimulationMetrics;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SimulationMetricsLoader {
    public static List<SimulationMetrics> loadFromCSV(String filename) {
        List<SimulationMetrics> metricsList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                SimulationMetrics metrics = getSimulationMetrics(line);
                metricsList.add(metrics);
            }
        } catch (IOException e) {
            log.error("Failed to load CSV: {}", (Object) e.getStackTrace());
        }
        return metricsList;
    }

    private static SimulationMetrics getSimulationMetrics(String line) {
        String[] parts = line.split(",");
        SimulationMetrics metrics = new SimulationMetrics();
        metrics.setTick(Integer.parseInt(parts[0]));
        metrics.setAvgEnergy(Double.parseDouble(parts[1]));
        metrics.setTotalBirths(Integer.parseInt(parts[2]));
        metrics.setTotalDeaths(Integer.parseInt(parts[3]));
        metrics.setHelperCount(Integer.parseInt(parts[4]));
        metrics.setSelfishCount(Integer.parseInt(parts[5]));
        metrics.setHelperBirths(Integer.parseInt(parts[6]));
        metrics.setSelfishBirths(Integer.parseInt(parts[7]));
        return metrics;
    }
}
