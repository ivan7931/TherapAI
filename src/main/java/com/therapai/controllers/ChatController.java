package com.therapai.controllers;

import com.therapai.servicies.ChatService;
import com.therapai.servicies.IAServer;
import com.therapai.utils.SceneManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

public class ChatController {
    //El @FXML debe llevarlo todo metodo o atributo que venga del fxml para que JavaFx lo detecte y los pueda conectar
    @FXML
    public VBox messagesBox;

    @FXML
    public TextField messageField1;

    @FXML
    public ScrollPane scrollPane;

    //Atributo para saber en que chat estamos.
    private String chatId;

    //Intermediario para mandar mensajes a la IA
    private ChatService chatService;

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

        //Se muestra el mensaje del usuario en el chat
        addMessageBox(message, true);
        messageField1.clear();

        //Se llama a Gemini
        String aiResponse = chatService.sendMessageToAI(message);
        System.out.println("Respuesta de la IA : " + aiResponse);
        aiResponse = aiResponse.replace("\n", System.lineSeparator());


        //Se muestra la respuesta de la IA
        addTypingMessage(aiResponse);

        //saveMessage(message, "user");
        //Falta comprobar que no sea sensitive
    }

    //Metodo para crear la caja donde se mostrara el mensaje en el chat.
    private void addMessageBox(String message, boolean isUser) {
        HBox box = new HBox();
        box.setPadding(new Insets(5));
        box.setSpacing(5);

        Label label = new Label(message);
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(label, Priority.ALWAYS);
        label.setPadding(new Insets(10));
        label.setStyle("-fx-background-radius: 10;");

        if (isUser) { //Si es del user el mensaje a la derecha
            box.setAlignment(Pos.CENTER_RIGHT);
            label.setStyle(label.getStyle() + "-fx-background-color: #3A6FF7; -fx-text-fill: white;");
        } else { //Si es de la IA a la izquierda
            box.setAlignment(Pos.CENTER_LEFT);
            label.setStyle(label.getStyle() + "-fx-background-color: #1E293B; -fx-text-fill: #E6F0FF;");
        }

        box.getChildren().add(label);

        Platform.runLater(() -> {
            messagesBox.getChildren().add(box);
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

    //Metodo para probar quer funciona correctamente
    public void testUI() {
        addMessageBox("Hola, soy un mensaje del usuario", true);
        addMessageBox("Hola, soy un mensaje del bot", false);
        addMessageBox("Este es un mensaje largo para comprobar que el wrap funciona correctamente y que la burbuja se adapta al tamaño del texto sin romper el diseño.", true);
        addMessageBox("Perfecto, funciona genial", false);
    }

    //Efecto para mostrar mensajes en el chat poco a poco
    private void addTypingMessage(String fullText) {
        HBox box = new HBox();
        box.setPadding(new Insets(5));
        box.setSpacing(5);
        box.setAlignment(Pos.CENTER_LEFT);

        TextFlow textFlow = new TextFlow();
        textFlow.setMaxWidth(600);
        textFlow.setPadding(new Insets(10));
        textFlow.setStyle("-fx-background-radius: 10; -fx-background-color: #1E293B;");
        textFlow.setLineSpacing(3);

        // Permitir que crezca verticalmente sin cortar
        HBox.setHgrow(textFlow, Priority.ALWAYS);

        box.getChildren().add(textFlow);

        Platform.runLater(() -> messagesBox.getChildren().add(box));

        final int[] index = {0};

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(20), e -> {
                    if (index[0] < fullText.length()) {
                        char c = fullText.charAt(index[0]);
                        Text t = new Text(String.valueOf(c));
                        t.setFill(Color.web("#E6F0FF"));
                        t.setStyle("-fx-font-size: 14px;");
                        textFlow.getChildren().add(t);
                        index[0]++;
                    }
                })
        );

        timeline.setCycleCount(fullText.length());
        timeline.play();
    }

    @FXML
    public void initialize() {
        //Hace que el autoScroll funcione correctamente, detecta cada vez que el VBox cambia de altura y baja el scroll del todo.
        messagesBox.heightProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> scrollPane.setVvalue(1.0));
        });

        //La APIKey habra que cambiarlo lo mas seguro, se queda sin tokens
        chatService = new ChatService(new IAServer("AIzaSyAiCXmfgyJz0ujR4UoZBR0kMNitmmrS5Mo"));

        testUI();
    }
}
