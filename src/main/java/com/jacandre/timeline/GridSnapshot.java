package com.jacandre.timeline;

import java.util.List;

public class GridSnapshot {
    public int tick;
    public List<RenderableEntity> entities;

    public GridSnapshot(int tick, List<RenderableEntity> entities) {
        this.tick = tick;
        this.entities = entities;
    }
}

