package com.jacandre.timeline;

import java.util.List;

public class GridSnapshot {
    public int tick;
    public List<GridEntityVisual> entities;

    public GridSnapshot(int tick, List<GridEntityVisual> entities) {
        this.tick = tick;
        this.entities = entities;
    }
}

