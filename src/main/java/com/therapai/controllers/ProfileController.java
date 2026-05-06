package com.therapai.controllers;

import com.therapai.utils.SceneManager;
import com.therapai.utils.Sesion;
import javafx.fxml.FXML;
import javafx.scene.control.Button;


public class ProfileController {
    @FXML
    private Button logoutButton;

    @FXML
    public void logOut(){
        //Borramos de memoria el usuario actual
        Sesion.setUsuario_actual(null);
        //Regresamos a la ventana de login
        //No tenemos que hacer llamadas a firestore porque no usamos firesotre
        //para el tema del login. Con firestore simplemente validamos que el
        //usario esté registrado--> Si registrado = dejamos que use app
        SceneManager.getInstance().switchTo("login.fxml");

    }
}
