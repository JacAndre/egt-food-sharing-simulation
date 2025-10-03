package com.jacandre.timeline;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GridCanvas extends Canvas {
    private static final int CELL_SIZE = 10;

    public GridCanvas(int gridSize) {
        super(gridSize * CELL_SIZE, gridSize * CELL_SIZE);
    }

    public void render(GridSnapshot snapshot) {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        for (GridEntityVisual entity : snapshot.entities) {
            Color color = switch (entity.type) {
                case "FOOD" -> Color.GREEN;
                case "AGENT" -> entity.strategy.equals("HELPER") ? Color.BLUE : Color.RED;
                default -> Color.GRAY;
            };

            gc.setFill(color);
            gc.fillRect(entity.x * CELL_SIZE, entity.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }
    }
}
