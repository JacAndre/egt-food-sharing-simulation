package com.jacandre.timeline;

import com.jacandre.models.TickSnapshot;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


@Getter
public class TickHistory {
    private final List<TickSnapshot> snapshots = new ArrayList<>();

    public void addSnapshot(TickSnapshot snapshot) {
        snapshots.add(snapshot);
    }

    public TickSnapshot getSnapshotAt(int tick) {
        return snapshots.stream()
                .filter(s -> s.tick() == tick)
                .findFirst()
                .orElse(null);
    }

    public int getTickCount() {
        return snapshots.size();
    }

    public boolean isEmpty() {
        return snapshots.isEmpty();
    }
}
