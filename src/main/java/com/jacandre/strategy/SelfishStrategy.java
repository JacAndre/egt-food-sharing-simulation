package com.jacandre.strategy;

import com.jacandre.core.Constants;
import com.jacandre.core.GridManager;
import com.jacandre.core.SimulationContext;
import com.jacandre.models.Agent;
import com.jacandre.models.Food;
import com.jacandre.models.GridEntity;

import java.awt.*;
import java.util.List;

public class SelfishStrategy implements AgentStrategy {

    @Override
    public void execute(Agent agent, GridManager grid, SimulationContext context) {
        Point pos = grid.getPositionOf(agent);
        if (pos == null) return;

        List<Point> neighbours = grid.getNeighbourPositions(pos, 1);

        // 1. Prioritise food
        for (Point p : neighbours) {
            GridEntity entity = grid.getEntityAt(p);
            if (entity instanceof Food) {
                grid.removeEntity(entity);
                grid.releasePosition(p);
                agent.increaseEnergy(Constants.FOOD_REWARD);

                if (grid.moveEntity(agent, p)) {
                    agent.decreaseEnergy(Constants.MOVE_COST);
                }
                return;
            }
        }

        // 2. Move to empty space
        List<Point> emptyNeighbours = grid.getEmptyNeighbours(pos, 1);
        if (!emptyNeighbours.isEmpty()) {
            Point target = emptyNeighbours.get(context.random().nextInt(emptyNeighbours.size()));
            if (grid.moveEntity(agent, target)) {
                agent.decreaseEnergy(Constants.MOVE_COST);
            }
        }
    }
}
