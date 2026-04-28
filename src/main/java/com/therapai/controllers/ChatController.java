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
        box.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(box, Priority.ALWAYS);

        TextFlow bubble = new TextFlow();
        bubble.setPadding(new Insets(10));
        bubble.setLineSpacing(3);
        bubble.setStyle("-fx-background-radius: 10;");

        if (isUser) {
            box.setAlignment(Pos.CENTER_RIGHT);
            bubble.setStyle(bubble.getStyle() + "-fx-background-color: #3A6FF7;");
        } else {
            box.setAlignment(Pos.CENTER_LEFT);
            bubble.setStyle(bubble.getStyle() + "-fx-background-color: #1E293B;");
        }

        Text text = new Text(message);
        text.setFill(isUser ? Color.WHITE : Color.web("#E6F0FF"));
        text.setStyle("-fx-font-size:14px;");

        bubble.getChildren().add(text);

        bubble.maxWidthProperty().bind(messagesBox.widthProperty().multiply(0.60));
        bubble.prefWidthProperty().bind(messagesBox.widthProperty().multiply(0.60));
        text.wrappingWidthProperty().bind(bubble.widthProperty().subtract(10));

        box.getChildren().add(bubble);

        Platform.runLater(() -> messagesBox.getChildren().add(box));
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
        box.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(box, Priority.ALWAYS);
        box.setAlignment(Pos.CENTER_LEFT);

        TextFlow bubble = new TextFlow();
        bubble.setPadding(new Insets(10));
        bubble.setLineSpacing(3);
        bubble.setStyle("-fx-background-color:#1E293B; -fx-background-radius:10;");

        bubble.maxWidthProperty().bind(messagesBox.widthProperty().multiply(0.60));
        bubble.prefWidthProperty().bind(messagesBox.widthProperty().multiply(0.60));

        Text text = new Text("");
        text.setFill(Color.web("#E6F0FF"));
        text.setStyle("-fx-font-size:14px;");
        text.wrappingWidthProperty().bind(bubble.widthProperty().subtract(10));

        bubble.getChildren().add(text);
        box.getChildren().add(bubble);

        Platform.runLater(() -> {
            messagesBox.getChildren().add(box);
            scrollPane.layout();
            scrollPane.setVvalue(1.0);
        });

        final int[] index = {0};

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(20), e -> {
            if (index[0] < fullText.length()) {
                text.setText(text.getText() + fullText.charAt(index[0]));
                index[0]++;
                bubble.requestLayout();
                box.requestLayout();
                Platform.runLater(() -> scrollPane.setVvalue(1.0));
            }
        }));

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
