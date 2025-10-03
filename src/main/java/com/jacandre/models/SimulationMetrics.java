package com.jacandre.models;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

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
        return String.format("%d,%.2f,%d,%d,%d,%d,%d,%d",
                tick, avgEnergy, totalBirths, totalDeaths,
                helperCount, selfishCount, helperBirths, selfishBirths);
    }
}