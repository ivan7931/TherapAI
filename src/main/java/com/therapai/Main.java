package com.therapai;

import com.therapai.servicies.FirebaseService;
import com.therapai.utils.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main extends Application {
    @Override
    public void start(Stage primaryStage){
        //Inicializamos el servicio de firebase para la persistencia de datos
        //Se inicializa aqui para asegurar que firestore esta disponible en cualquier momento
        //que queramos realizar operaciones sobre los datos
        FirebaseService.inicializar();
        SceneManager.getInstance().setMainStage(primaryStage);
        SceneManager.getInstance().switchTo("login.fxml");
    }
    public static void main(String[] args) {
        launch(args);
    }
}