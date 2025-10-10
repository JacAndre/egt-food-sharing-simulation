package com.jacandre.strategy;

import com.jacandre.core.Constants;
import com.jacandre.core.GridManager;
import com.jacandre.core.SimulationContext;
import com.jacandre.models.Agent;
import com.jacandre.models.GridEntity;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
public class HelperStrategy implements AgentStrategy {

    @Override
    public void execute(Agent agent, GridManager grid, SimulationContext context) {
        Point pos = grid.getPositionOf(agent);
        if (pos == null) return;

        // 1. Scan for low-energy agents within vision radius
        List<Point> candidates = grid.getEntitiesOfType(pos, Constants.VISION_RADIUS, Agent.class);
        Optional<Point> nearestLowEnergy = candidates.stream()
                .filter(p -> {
                    GridEntity entity = grid.getEntityAt(p);
                    return entity instanceof Agent other && other.getEnergy() < Constants.LOW_ENERGY_THRESHOLD;
                })
                .min(Comparator.comparingDouble(pos::distanceSq));

        if (nearestLowEnergy.isPresent()) {
            Point target = grid.stepToward(pos, nearestLowEnergy.get());
            GridEntity entity = grid.getEntityAt(nearestLowEnergy.get());

            if (entity instanceof Agent other && pos.distance(nearestLowEnergy.get()) <= 1.0) {
                double transfer = Constants.ASSIST_COST;

                if (agent.getEnergy() > transfer) {
                    agent.decreaseEnergy(transfer);
                    other.increaseEnergy(transfer);
                    log.info("Agent {} assisted Agent {} with {} energy.", agent.getId(), other.getId(), transfer);
                } else {
                    log.debug("Agent {} attempted to assist but lacked sufficient energy.", agent.getId());
                }
                return;
            }

            if (!grid.isOccupied(target) && grid.moveEntity(agent, target)) {
                agent.decreaseEnergy(Constants.MOVE_COST);
                return;
            }
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
