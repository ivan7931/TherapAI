package com.therapai.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {

    private static SceneManager instance;
    private Stage mainStage;

    private SceneManager(){}

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
    }

    public static SceneManager getInstance() {
        if (instance == null){
            instance = new SceneManager();
        }
        return instance;
    }

    //Metodo para cambiar de screen
    public void switchTo(String fxml){
        try{
            //La ruta hay que cambiarla.
            Parent root = FXMLLoader.load(getClass().getResource("/home/aspa/IdeaProjects/therapai/src/main/java/org/example/views/" + fxml));
            Scene scene = new Scene(root);
            mainStage.setScene(scene);
            mainStage.show();
        } catch (IOException e) {
            throw new RuntimeException(e); //Hay que cambiarla por una personalizada.
        }
    }
}
