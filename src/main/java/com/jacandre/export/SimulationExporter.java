package com.jacandre.export;

import com.jacandre.models.*;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@Slf4j
public class SimulationExporter {
    public static void exportGridSnapshots(String filename, List<TickSnapshot> snapshots, GridManager gridManager) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("tick,x,y,energy,strategy,type");

            for (TickSnapshot snapshot : snapshots) {
                for (Map.Entry<Point, GridEntity> entry : snapshot.gridState().entrySet()) {
                    Point pos = entry.getKey();
                    GridEntity entity = entry.getValue();

                    if (entity instanceof Agent agent) {
                        writer.printf("%d,%d,%d,%.2f,%s,AGENT%n",
                                snapshot.tick(), pos.x, pos.y,
                                agent.getEnergy(), agent.getStrategy().name());
                    } else if (entity instanceof Food) {
                        writer.printf("%d,%d,%d,,,FOOD%n",
                                snapshot.tick(), pos.x, pos.y);
                    }
                }
            }

        } catch (IOException e) {
            log.error("Failed to export grid snapshots", e);
        }
    }
}
