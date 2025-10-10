package com.jacandre.strategy;

import com.jacandre.core.Constants;
import com.jacandre.core.GridManager;
import com.jacandre.core.SimulationContext;
import com.jacandre.models.Agent;
import com.jacandre.models.GridEntity;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.util.List;

@Slf4j
public class HelperStrategy implements AgentStrategy {

    @Override
    public void execute(Agent agent, GridManager grid, SimulationContext context) {

        Point pos = grid.getPositionOf(agent);
        if (pos == null) return;

        List<Point> neighbours = grid.getNeighbourPositions(pos, 1);

        // 1. Assist nearby low-energy agents
        for (Point p : neighbours) {
            GridEntity entity = grid.getEntityAt(p);
            if (entity instanceof Agent other && other.getEnergy() < Constants.LOW_ENERGY_THRESHOLD) {
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
        }

        // 2. Move to empty space if no one to help
        List<Point> emptyNeighbours = grid.getEmptyNeighbours(pos, 1);
        if (!emptyNeighbours.isEmpty()) {
            Point target = emptyNeighbours.get(context.random().nextInt(emptyNeighbours.size()));
            if (grid.moveEntity(agent, target)) {
                agent.decreaseEnergy(Constants.MOVE_COST);
            }
        }
    }
}
