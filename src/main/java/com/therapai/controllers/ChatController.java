package com.therapai.controllers;

import com.therapai.utils.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ChatController {
    //El @FXML debe llevarlo todo metodo o atributo que venga del fxml para que JavaFx lo detecte y los pueda conectar
    @FXML
    public VBox messagesBox;

    @FXML
    public TextField messageField1;

    //Atributo para saber en que chat estamos.
    private String chatId;

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

    @FXML
    public void sendMessage() {
        String message = messageField1.getText().trim();
        if (message.isEmpty()) return;
        messageField1.clear();
        saveMessage(message, "user");
        autoScroll();
        //Falta comprobar que no sea sensitive y llamar a la IA
    }

    //Metodo para crear la caja donde se mostrara el mensaje en el chat.
    private void addMessageBox(String message, boolean isUser) {

    }

    private void callGemini(String userMessage) {
        String aiResponse = "Prueba";

        addMessageBox(aiResponse, false);
        saveMessage(aiResponse, "bot");
        autoScroll();
    }

    //Con este metodo al enviar o recibir un mensaje en el chat se hace el autoscroll hacia abajo.
    private void autoScroll() {
        Platform.runLater(() -> {
            messagesBox.layout();
            messagesBox.getParent().layout();
        });
    }

    //Metodo para cargar el chat
    protected void loadChat(String chatId) {
        this.chatId = chatId;
        loadMessages();
    }

    //Metodo para cargar los mensajes de la conversacion desde la bdd.
    private void loadMessages() {

    }

    private void saveMessage(String message, String sender) {

    }

    //Este metodo serviria para controlar los mensajes si contienen algo que no deben o podemos hacerlo diciendoselo a la IA que lo controle,
    //pero con el metodo podemos mostrar un mensaje al user si eso ocurre o incluso si lo hace varias veces hacer algo distitno.
    private void detectSensitiveContent(String message) {
        boolean isSensitive = message.contains("ESO");
    }
}
