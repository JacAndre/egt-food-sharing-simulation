package com.jacandre.visualisation;

import com.jacandre.timeline.SimulationMetrics;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

@Slf4j
public class MetricsDashboard extends Application {
    @Override
    public void start(Stage stage) {
        stage.setTitle("Simulation Metrics");

        List<SimulationMetrics> metricsHistory = SimulationMetricsLoader.loadFromCSV("simulation_metrics.csv");

        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
                new Tab("Agent Count", plotHelperVsSelfish(metricsHistory)),
                new Tab("Energy", plotEnergyOverTime(metricsHistory)),
                new Tab("Births/Deaths", plotBirthsVsDeaths(metricsHistory)),
                new Tab("Strategy Births", plotStrategyBirths(metricsHistory)),
                new Tab("Strategy Ratio", plotStrategyRatio(metricsHistory)),
                new Tab("Energy Distribution", plotEnergyDistribution(metricsHistory))
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

    private LineChart<Number, Number> plotStrategyBirths(List<SimulationMetrics> metricsHistory) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Tick");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Births");

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("HELPER vs SELFISH Births");

        XYChart.Series<Number, Number> helperBirths = new XYChart.Series<>();
        helperBirths.setName("HELPER Births");

        XYChart.Series<Number, Number> selfishBirths = new XYChart.Series<>();
        selfishBirths.setName("SELFISH Births");

        for (SimulationMetrics metrics : metricsHistory) {
            helperBirths.getData().add(new XYChart.Data<>(metrics.getTick(), metrics.getHelperBirths()));
            selfishBirths.getData().add(new XYChart.Data<>(metrics.getTick(), metrics.getSelfishBirths()));
        }

        Collections.addAll(chart.getData(), helperBirths, selfishBirths);
        return chart;
    }

    private LineChart<Number, Number> plotStrategyRatio(List<SimulationMetrics> metricsHistory) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Tick");

        NumberAxis yAxis = new NumberAxis(0, 100, 10);
        yAxis.setLabel("Percentage");

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Strategy Ratio Over Time");

        XYChart.Series<Number, Number> helperRatio = new XYChart.Series<>();
        helperRatio.setName("HELPER %");

        XYChart.Series<Number, Number> selfishRatio = new XYChart.Series<>();
        selfishRatio.setName("SELFISH %");

        for (SimulationMetrics metrics : metricsHistory) {
            int total = metrics.getHelperCount() + metrics.getSelfishCount();
            if (total > 0) {
                double helperPercent = 100.0 * metrics.getHelperCount() / total;
                double selfishPercent = 100.0 * metrics.getSelfishCount() / total;
                helperRatio.getData().add(new XYChart.Data<>(metrics.getTick(), helperPercent));
                selfishRatio.getData().add(new XYChart.Data<>(metrics.getTick(), selfishPercent));
            }
        }

        Collections.addAll(chart.getData(), helperRatio, selfishRatio);
        return chart;
    }

    public VBox plotEnergyDistribution(List<SimulationMetrics> metricsHistory) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Energy Range");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Agent Count");

        BarChart<String, Number> histogram = new BarChart<>(xAxis, yAxis);
        histogram.setTitle("Energy Distribution Over Time");
        histogram.setAnimated(false);

        // Slider to control tick
        Slider tickSlider = new Slider(0, metricsHistory.size() - 1, 0);
        tickSlider.setMajorTickUnit(1);
        tickSlider.setSnapToTicks(true);
        tickSlider.setShowTickLabels(true);
        tickSlider.setShowTickMarks(true);

        Label tickLabel = new Label("Tick: 0");

        // Listener to update chart
        tickSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int tickIndex = newVal.intValue();
            tickLabel.setText("Tick: " + tickIndex);
            SimulationMetrics metrics = metricsHistory.get(tickIndex);
            updateHistogram(histogram, metrics);
        });

        // Play button
        Button playButton = new Button("Play");
        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(200), event -> {
            int currentTick = (int) tickSlider.getValue();
            if (currentTick < metricsHistory.size() - 1) {
                tickSlider.setValue(currentTick + 1);
            } else {
                timeline.stop();
                playButton.setText("Play");
            }
        });
        timeline.getKeyFrames().add(frame);

        playButton.setOnAction(e -> {
            if (timeline.getStatus() == Animation.Status.RUNNING) {
                timeline.stop();
                playButton.setText("Play");
            } else {
                timeline.play();
                playButton.setText("Pause");
            }
        });

        // Refresh button
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> {
            tickSlider.setValue(0);
            updateHistogram(histogram, metricsHistory.getFirst());
            tickLabel.setText("Tick: 0");
        });

        updateHistogram(histogram, metricsHistory.getFirst());

        HBox controls = new HBox(10, tickLabel, tickSlider, playButton, refreshButton);
        controls.setAlignment(Pos.CENTER);

        VBox layout = new VBox(10, histogram, controls);
        layout.setPadding(new Insets(10));

        return layout;
    }


    private void updateHistogram(BarChart<String, Number> chart, SimulationMetrics metrics) {
        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Tick " + metrics.getTick());

        int binSize = 10;
        int binCount = 10;
        int[] bins = new int[binCount];

        for (double energy : metrics.getEnergySnapshot()) {
            int index = Math.min((int)(energy / binSize), binCount - 1);
            bins[index]++;
        }

        for (int i = 0; i < bins.length; i++) {
            String label = (i * binSize) + "-" + ((i + 1) * binSize);
            series.getData().add(new XYChart.Data<>(label, bins[i]));
        }

        chart.getData().add(series);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

