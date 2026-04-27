package com.therapai.controllers;

import com.therapai.utils.SceneManager;
import javafx.fxml.FXML;

public class LoginController {
    @FXML
    public void login() {

    }

    @FXML
    public void goRegister() {
        SceneManager.getInstance().switchTo("register.fxml");
    }
}
