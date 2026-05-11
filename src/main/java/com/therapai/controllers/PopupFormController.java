package com.therapai.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.function.Consumer;

public class PopupFormController {

    @FXML
    public Label titleLabel;

    @FXML
    public TextField field1;   // Campo principal

    @FXML
    public PasswordField field2; // Campo opcional (solo para contraseña)

    @FXML
    public Button cancelButton;

    @FXML
    public Button confirmButton;

    private Consumer<String[]> onConfirm;

    public void setOnConfirm(Consumer<String[]> action) {
        this.onConfirm = action;
    }

    public void enableConfirmField(boolean enabled) {
        field2.setVisible(enabled);
        field2.setManaged(enabled);
    }

    @FXML
    private void initialize() {
        cancelButton.setOnAction(e -> cancelButton.getScene().getWindow().hide());

        confirmButton.setOnAction(e -> {
            if (onConfirm != null) {
                onConfirm.accept(new String[]{
                        field1.getText(),
                        field2.getText()
                });
            }
            confirmButton.getScene().getWindow().hide();
        });
    }
}
