package com.jacandre.timeline;

import java.io.*;
import java.util.*;

public class CSVLoader {
    public static List<GridSnapshot> loadSnapshots(String filename) throws IOException {
        Map<Integer, List<GridEntityVisual>> tickMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                int tick = Integer.parseInt(parts[0]);
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                double energy = parts[3].isEmpty() ? 0.0 : Double.parseDouble(parts[3]);
                String strategy = parts[4];
                String type = parts[5];

                GridEntityVisual entity = new GridEntityVisual();
                entity.x = x;
                entity.y = y;
                entity.energy = energy;
                entity.strategy = strategy;
                entity.type = type;

                tickMap.computeIfAbsent(tick, t -> new ArrayList<>()).add(entity);
            }
        }

        List<GridSnapshot> snapshots = new ArrayList<>();
        for (Map.Entry<Integer, List<GridEntityVisual>> entry : tickMap.entrySet()) {
            snapshots.add(new GridSnapshot(entry.getKey(), entry.getValue()));
        }

        snapshots.sort(Comparator.comparingInt(s -> s.tick));
        return snapshots;
    }
}
