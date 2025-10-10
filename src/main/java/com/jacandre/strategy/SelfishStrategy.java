package com.jacandre.strategy;

import com.jacandre.core.Constants;
import com.jacandre.core.GridManager;
import com.jacandre.core.SimulationContext;
import com.jacandre.models.Agent;
import com.jacandre.models.Food;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.util.List;
import java.util.Optional;

@Slf4j
public class SelfishStrategy implements AgentStrategy {

    @Override
    public void execute(Agent agent, GridManager grid, SimulationContext context) {
        Point pos = grid.getPositionOf(agent);
        if (pos == null) return;

        // 1. Seek food within vision radius
        List<Point> foodPositions = grid.getEntitiesOfType(pos, Constants.VISION_RADIUS, Food.class);
        Optional<Point> nearestFood = grid.findNearest(pos, foodPositions);

        if (nearestFood.isPresent()) {
            Point target = grid.stepToward(pos, nearestFood.get());

            if (grid.isOccupied(target)) {
                if (grid.tryConsumeFood(target, agent)) {
                    if (grid.moveEntity(agent, target)) {
                        agent.decreaseEnergy(Constants.MOVE_COST);
                    }
                    return;
                }
            }

            grid.moveEntity(agent, target);
            agent.decreaseEnergy(Constants.MOVE_COST);

            return;
        }


        // 2. Fallback to random movement
        List<Point> emptyNeighbours = grid.getEmptyNeighbours(pos, 1);
        if (!emptyNeighbours.isEmpty()) {
            Point target = emptyNeighbours.get(context.random().nextInt(emptyNeighbours.size()));
            if (grid.moveEntity(agent, target)) {
                agent.decreaseEnergy(Constants.MOVE_COST);
            }
        }
    }
}
