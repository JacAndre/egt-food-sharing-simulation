package com.jacandre.timeline;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Slf4j
public class SimulationMetrics {
    int tick;
    double avgEnergy;
    int totalBirths;
    int totalDeaths;
    int helperCount;
    int selfishCount;
    int helperBirths;
    int selfishBirths;
    private List<Double> energySnapshot;

    public SimulationMetrics() {

    }

    public void logMetrics() {
        log.info("Tick {}: Avg energy = {}, Births = {}, Deaths = {}",
                tick, avgEnergy, totalBirths, totalDeaths);
        log.info("Strategy breakdown: HELPERS = {}, SELFISH = {}",
                helperCount, selfishCount);
        log.info("Birth breakdown: HELPERS = {}, SELFISH = {}",
                helperBirths, selfishBirths);
    }

    public String toCSVRow() {
        String snapshotString = energySnapshot == null || energySnapshot.isEmpty() ? "" :
                energySnapshot.stream()
                        .map(e -> String.format("%.2f", e))
                        .collect(Collectors.joining(","));

        return String.format("%d,%.2f,%d,%d,%d,%d,%d,%d,\"%s\"",
                tick, avgEnergy, totalBirths, totalDeaths,
                helperCount, selfishCount, helperBirths, selfishBirths, snapshotString);
    }
}