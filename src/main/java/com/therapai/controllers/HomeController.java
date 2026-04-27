package com.therapai.controllers;

import com.therapai.models.ConversationModel;
import com.therapai.utils.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import javax.swing.*;

public class HomeController {
    @FXML
    private ListView<ConversationModel> chatList;

    @FXML
    public void newChat() {

    }

    //Metodo para abrir la conversacion que selecciono de la lista.
    @FXML
    public void openChat() {
        String chatId = chatList.getSelectionModel().getSelectedItem().getChatId();

        //Con el getInstance se obtiene el SceneManager que es general para toda la app,
        // el metodo switchToWithController nos cambia a la pantalla que pasamos y nos devuelve el controller de esa scene y
        //asi podemos pasar parametros entre pantallas.
        ChatController chatController = SceneManager.getInstance().switchToWithController("chat.fxml");

        chatController.loadChat(chatId);
    }

    //Los metodos go son para cambiar a esa pantalla.
    @FXML
    public void goHome() {
        SceneManager.getInstance().switchTo("home.fxml");
    }

    @FXML
    public void goHistory() {
        SceneManager.getInstance().switchTo("history.fxml");
    }

    @FXML
    public void goSettings() {
        SceneManager.getInstance().switchTo("settings.fxml");
    }

    @FXML
    public void goProfile() {
        SceneManager.getInstance().switchTo("profile.fxml");
    }
}
