package com.therapai.components;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.animation.Timeline;
import javafx.util.Duration;


public class TypingBubble extends HBox {
    private final Label dots = new Label(".");
    private final Timeline timeline;

    public TypingBubble() {
        this.setStyle("-fx-background-color: #1E293B; -fx-padding: 8 12; -fx-background-radius: 12;");
        dots.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        this.setAlignment(Pos.CENTER_LEFT);
        this.getChildren().add(dots);

        timeline = new Timeline(
                new KeyFrame(Duration.seconds(0.0), e -> dots.setText(".")),
                new KeyFrame(Duration.seconds(0.3), e -> dots.setText("..")),
                new KeyFrame(Duration.seconds(0.6), e -> dots.setText("..."))
        );

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    public void stop() {
        timeline.stop();
    }
}
