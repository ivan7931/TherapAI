package com.therapai.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class PopupConfirmController {

    @FXML
    public Label titleLabel;
    @FXML
    public Label messageLabel;
    @FXML
    public Button cancelButton;
    @FXML
    public Button confirmButton;

    private Runnable onConfirm;

    public void setOnConfirm(Runnable action) {
        this.onConfirm = action;
    }

    @FXML
    private void initialize() {
        cancelButton.setOnAction(e -> cancelButton.getScene().getWindow().hide());
        confirmButton.setOnAction(e -> {
            if (onConfirm != null) onConfirm.run();
            confirmButton.getScene().getWindow().hide();
        });
    }
}
