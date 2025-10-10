# Agent-Based Simulation Framework

A modular, thread-safe agent-based simulation written in Java. Agents navigate a toroidal grid, seek food, assist neighbours, reproduce, and evolve over time. Designed for extensibility, clarity, and performance.

---

## Build & Run

# Clone the repo
```shell
git clone https://github.com/your-username/agent-simulation.git```
```

## 🚀 Features

- **GridManager**: Thread-safe spatial manager with fine-grained cell locking for concurrent movement and interaction.
- **Agent Strategies**:
    - `SelfishStrategy`: Seeks and consumes food.
    - `HelperStrategy`: Shares energy with low-energy neighbours.
- **Survival Arbitration**: When multiple agents target the same food, the highest-energy agent wins.
- **Simulation Lifecycle**: Agents act, reproduce, and die based on energy and strategic behaviour.
- **Perception & Movement**: Agents scan their surroundings and move purposefully using a configurable vision radius.
- **Exit Strategies**:
    - Tick limit
- **Metrics & Snapshots**: Tracks births, deaths, energy levels, and strategy counts per tick.

---

## Architecture Overview

```text
+-------------------+       +-------------------+
|   Simulation.java |<----->|   GridManager     |
+-------------------+       +-------------------+
        |                          |
        v                          v
+-------------------+       +-------------------+
| AgentStrategy     |       | GridEntity        |
| (Selfish, Helper) |       | (Agent, Food)     |
+-------------------+       +-------------------+

```
## Project Structure
```text
src/
├── core/             # GridManager, SimulationContext, Constants
├── models/           # Agent, Food, GridEntity
├── strategy/         # SelfishStrategy, HelperStrategy
├── metrics/          # SimulationMetrics, snapshot tracking
└── Simulation.java   # Main loop and lifecycle
```


## Configuration
Modify Constants.java to tune:

- Vision radius
- Energy thresholds
- Reproduction cost
- Food expiry
- Tick limits

## Metrics Tracked
- Tick count
- Average energy
- Total deaths
- Births per strategy
- Agent counts per strategy
- Energy distribution snapshot

## Visualization
Two JavaFX visualizers:
- **GridVisualizer**: Real-time grid display of agents and food.
- **ChartVisualizer**: Dynamic charts of population and energy trends.

Both can be run via Maven profiles.
To run the visualizers, use the following commands:

```shell
mvn javafx:run -Pgrid
```
```shell
mvn javafx:run -Pchart
```

## Testing
Unit tests cover core functionalities, including movement, interaction, reproduction, and death. Run tests with
```shell
mvn test
```
