package com.jacandre.strategy;

import com.jacandre.core.GridManager;
import com.jacandre.core.SimulationContext;
import com.jacandre.models.Agent;

public interface AgentStrategy {
    void execute(Agent agent, GridManager grid, SimulationContext context);
}
