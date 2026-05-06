package com.therapai.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

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

    //Metodo para cambiar de screen.
    public void switchTo(String fxml){
        try{
            //La ruta hay que cambiarla.
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/therapai/views/" + fxml)));
            Scene scene = new Scene(root);
            mainStage.setScene(scene);
            mainStage.centerOnScreen();
            mainStage.show();
        } catch (IOException e) {
            throw new RuntimeException(e); //Hay que cambiarla por una personalizada.
        }
    }

    //Con este metodo podemos cambiar de pantalla devolviendo el controller de la pagina a la que vamos a cambiar
    // para poder pasarle parametros entre pantallas despues pej cuando selecciona un chat para abrirlo.
    public <T> T switchToWithController(String fxml){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/therapai/views/" + fxml));
            Parent root = loader.load();
            mainStage.setScene(new Scene(root));
            mainStage.show();
            return loader.getController();
        }catch (Exception e){
            e.printStackTrace(); //Hay que cambiarla por una personalizada.
            return null;
        }
    }
}
