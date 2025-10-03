package com.jacandre.visualisation;

import com.jacandre.models.SimulationMetrics;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import java.util.Collections;
import java.util.List;

public class ChartApp extends Application {
    @Override
    public void start(Stage stage) {
        stage.setTitle("Simulation Metrics");

        List<SimulationMetrics> metricsHistory = SimulationMetricsLoader.loadFromCSV("simulation_metrics.csv");

        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
                new Tab("Agent Count", plotHelperVsSelfish(metricsHistory)),
                new Tab("Energy", plotEnergyOverTime(metricsHistory)),
                new Tab("Births/Deaths", plotBirthsVsDeaths(metricsHistory))
        );

        Scene scene = new Scene(tabPane, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    private LineChart<Number, Number> plotHelperVsSelfish(List<SimulationMetrics> metricsHistory) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Tick");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Agent Count");

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("HELPER vs SELFISH Agents");

        XYChart.Series<Number, Number> helperSeries = new XYChart.Series<>();
        helperSeries.setName("HELPER");

        XYChart.Series<Number, Number> selfishSeries = new XYChart.Series<>();
        selfishSeries.setName("SELFISH");

        for (SimulationMetrics metrics : metricsHistory) {
            helperSeries.getData().add(new XYChart.Data<>(metrics.getTick(), metrics.getHelperCount()));
            selfishSeries.getData().add(new XYChart.Data<>(metrics.getTick(), metrics.getSelfishCount()));
        }

        Collections.addAll(lineChart.getData(), helperSeries, selfishSeries);
        return lineChart;
    }

    private LineChart<Number, Number> plotEnergyOverTime(List<SimulationMetrics> metricsHistory) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Tick");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Avg Energy");

        LineChart<Number, Number> energyChart = new LineChart<>(xAxis, yAxis);
        energyChart.setTitle("Average Energy Over Time");

        XYChart.Series<Number, Number> energySeries = new XYChart.Series<>();
        energySeries.setName("Avg Energy");

        for (SimulationMetrics metrics : metricsHistory) {
            energySeries.getData().add(new XYChart.Data<>(metrics.getTick(), metrics.getAvgEnergy()));
        }

        Collections.addAll(energyChart.getData(), energySeries);
        return energyChart;
    }

    private LineChart<Number, Number> plotBirthsVsDeaths(List<SimulationMetrics> metricsHistory) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Tick");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Count");

        LineChart<Number, Number> birthDeathChart = new LineChart<>(xAxis, yAxis);
        birthDeathChart.setTitle("Births vs Deaths");

        XYChart.Series<Number, Number> birthSeries = new XYChart.Series<>();
        birthSeries.setName("Total Births");

        XYChart.Series<Number, Number> deathSeries = new XYChart.Series<>();
        deathSeries.setName("Total Deaths");

        for (SimulationMetrics metrics : metricsHistory) {
            birthSeries.getData().add(new XYChart.Data<>(metrics.getTick(), metrics.getTotalBirths()));
            deathSeries.getData().add(new XYChart.Data<>(metrics.getTick(), metrics.getTotalDeaths()));
        }

        Collections.addAll(birthDeathChart.getData(), birthSeries, deathSeries);
        return birthDeathChart;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

