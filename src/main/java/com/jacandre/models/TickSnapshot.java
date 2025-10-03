package com.jacandre.models;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public record TickSnapshot(int tick, Map<Point, GridEntity> gridState) {
    public TickSnapshot(int tick, Map<Point, GridEntity> gridState) {
        this.tick = tick;
        this.gridState = new HashMap<>(gridState);
    }
}
