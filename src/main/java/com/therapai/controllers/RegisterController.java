package com.therapai.controllers;

import com.therapai.utils.SceneManager;
import javafx.fxml.FXML;

public class RegisterController {
    @FXML
    public void register() {

    }

    @FXML
    public void goLogin(){
        SceneManager.getInstance().switchTo("login.fxml");
    }
}
