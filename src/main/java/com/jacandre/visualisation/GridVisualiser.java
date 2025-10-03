package com.jacandre.visualisation;

import com.jacandre.core.Constants;
import com.jacandre.timeline.CSVLoader;
import com.jacandre.timeline.GridSnapshot;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

public class GridVisualiser extends Application {
    private List<GridSnapshot> snapshots;
    private SimulationCanvas canvas;
    private int currentTick = 0;

    static class Delta { double x, y; }

    @Override
    public void start(Stage stage) throws Exception {
        snapshots = CSVLoader.loadSnapshots("simulation_grid_snapshots.csv");

        canvas = new SimulationCanvas(Constants.GRID_SIZE);
        canvas.render(snapshots.get(currentTick));

        Slider tickSlider = new Slider(0, snapshots.size() - 1, 0);
        tickSlider.setMajorTickUnit(1);
        tickSlider.setSnapToTicks(true);
        tickSlider.setShowTickLabels(true);

        tickSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            currentTick = newVal.intValue();
            canvas.render(snapshots.get(currentTick));
        });

        Button playPauseButton = new Button("Play");

        Timeline playbackTimeline = new Timeline();
        playbackTimeline.setCycleCount(Timeline.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(100), e -> {
            int nextTick = (int) (tickSlider.getValue() + 1);
            if (nextTick < snapshots.size()) {
                tickSlider.setValue(nextTick);
            } else {
                playbackTimeline.stop();
                playPauseButton.setText("Play");
            }
        });

        playPauseButton.setOnAction(e -> {
            if (playbackTimeline.getStatus() == Animation.Status.RUNNING) {
                playbackTimeline.stop();
                playPauseButton.setText("Play");
            } else {
                playbackTimeline.play();
                playPauseButton.setText("Pause");
            }
        });

        playbackTimeline.getKeyFrames().add(frame);

        Group canvasGroup = new Group(canvas);
        ScrollPane scrollPane = new ScrollPane(canvasGroup);
        scrollPane.setPannable(true);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setCenter(scrollPane);      // canvas in the center

        HBox mediaBar = new HBox(10, tickSlider, playPauseButton);
        mediaBar.setPadding(new Insets(10));
        mediaBar.setAlignment(Pos.CENTER);

        root.setBottom(mediaBar);

        canvas.setOnScroll(event -> {
            double zoomFactor = event.getDeltaY() > 0 ? 1.1 : 0.9;
            canvas.setScaleX(canvas.getScaleX() * zoomFactor);
            canvas.setScaleY(canvas.getScaleY() * zoomFactor);
        });

        final Delta dragDelta = new Delta();

        canvas.setOnMousePressed(event -> {
            dragDelta.x = event.getX();
            dragDelta.y = event.getY();
        });

        canvas.setOnMouseDragged(event -> {
            double dx = event.getX() - dragDelta.x;
            double dy = event.getY() - dragDelta.y;
            canvas.setTranslateX(canvas.getTranslateX() + dx);
            canvas.setTranslateY(canvas.getTranslateY() + dy);
            dragDelta.x = event.getX();
            dragDelta.y = event.getY();
        });

        int windowWidth = 800;
        int windowHeight = 850;

        Scene scene = new Scene(root, windowWidth, windowHeight);
        stage.setScene(scene);
        stage.setTitle("Grid Visualiser");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
