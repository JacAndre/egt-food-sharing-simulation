package com.jacandre.visualisation;

import com.jacandre.models.SimulationMetrics;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class SimulationMetricsLoader {
    public static List<SimulationMetrics> loadFromCSV(String filename) {
        List<SimulationMetrics> metricsList = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filename))) {
            reader.readNext(); // skip header
            String[] parts;
            while ((parts = reader.readNext()) != null) {
                SimulationMetrics metrics = getSimulationMetrics(parts);
                metricsList.add(metrics);
            }
        } catch (IOException | CsvValidationException e) {
            log.error("Failed to load CSV: {}", (Object) e.getStackTrace());
        }
        return metricsList;
    }

    private static SimulationMetrics getSimulationMetrics(String[] parts) {
        SimulationMetrics metrics = new SimulationMetrics();
        metrics.setTick(Integer.parseInt(parts[0]));
        metrics.setAvgEnergy(Double.parseDouble(parts[1]));
        metrics.setTotalBirths(Integer.parseInt(parts[2]));
        metrics.setTotalDeaths(Integer.parseInt(parts[3]));
        metrics.setHelperCount(Integer.parseInt(parts[4]));
        metrics.setSelfishCount(Integer.parseInt(parts[5]));
        metrics.setHelperBirths(Integer.parseInt(parts[6]));
        metrics.setSelfishBirths(Integer.parseInt(parts[7]));

        // Rejoin all remaining parts for snapshot
        String snapshotRaw = String.join(",", Arrays.copyOfRange(parts, 8, parts.length));
        metrics.setEnergySnapshot(parseEnergySnapshot(snapshotRaw));

        log.info("Raw snapshot string: {}", snapshotRaw);
        log.info("Parsed snapshot size: {}", metrics.getEnergySnapshot().size());

        return metrics;
    }

    private static List<Double> parseEnergySnapshot(String snapshotString) {
        if (snapshotString == null || snapshotString.isEmpty()) return Collections.emptyList();

        snapshotString = snapshotString.replaceAll("^\"|\"$", "");

        return Arrays.stream(snapshotString.split(","))
                .map(String::trim)
                .map(s -> s.isEmpty() ? 0.0 : Double.parseDouble(s)) // If position is empty, fill with placeholder to avoid misaligned plotting
                .collect(Collectors.toList());
    }
}
