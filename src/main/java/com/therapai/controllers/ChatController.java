package com.therapai.controllers;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.therapai.servicies.ChatService;
import com.therapai.servicies.FirebaseService;
import com.therapai.servicies.IAServer;
import com.therapai.utils.SceneManager;
import com.therapai.utils.Sesion;
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

import java.util.HashMap;
import java.util.Map;

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

    /***
     * Enviar mensaje y obtener respuesta de la IA
     */
    @FXML
    public void sendMessage() {
        //recuperamos el texto que ha escrito el usuario -> si vacio nada
        String message = messageField1.getText().trim();
        if (message.isEmpty()) return;

        //Si es la primera vez que se envia un mensaje -> creamos nuevo chat en firestore
        crearChat();

        //Se muestra el mensaje del usuario en la UI
        addMessageBox(message, true);
        //guardamos el mensaje que envia el usuario en firestore
        saveMessage(message,"user");
        //limpiamos campos
        messageField1.clear();

        //Se llama a la api de gemini para obtener respuestas
        String aiResponse = chatService.sendMessageToAI(message);
        System.out.println("Respuesta de la IA : " + aiResponse);
        aiResponse = aiResponse.replace("\n", System.lineSeparator());

        //Si IA no responde-> mosntramos mensaje infomrando al usuario en la UI Y guardamos ese mensaje en firestore tambien
        if(aiResponse==null || aiResponse.contains("Error") || aiResponse.contains("ERROR") ||aiResponse.contains("error")){
            addTypingMessage(aiResponse);
            saveMessage("servicio de IA no disponible", "IA");
            return;
        }
        //Hay respuesta de la IA , guardamos mensaje
        addTypingMessage(aiResponse);
        saveMessage(aiResponse, "IA");
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
        System.out.println("cargando mensajes ....");
        //Instancia de la base de datos
        Firestore db = FirebaseService.getDb();
        //Recueramos el localId del usuario actual
        String uid = Sesion.getUserId();
        System.out.println("id del chat->"+chatId);
        System.out.println("id del user->"+uid);
        //Referenciamos la coleccion mensajes de la base de datos de FireStore del chat actual
        //para ello tenemos que llegas a la coleccion de mensajes de la siguiente manera :
        ApiFuture<QuerySnapshot> future = db.collection("users")
                .document(uid)
                .collection("chats")
                .document(chatId)
                .collection("messages")
                //ordenamos los mensajes del mas antigui al mas reciente
                .orderBy("timestamp", Query.Direction.ASCENDING)
                //recuperamos la coeccion
                .get();
        //Ejecutamos un hilo en segundo plano para no bloquear javafx
        new Thread(() -> {
            try{
                //Obtenemos los resultados
                QuerySnapshot snapshot = future.get();

                //Limpiamos la pantalla de chats antes de cargar los mensajes para evitar que ya hubiera mensajes previos
                Platform.runLater(() -> {
                    messagesBox.getChildren().clear();
                });
                //Recorremos cada mensaje
                for(QueryDocumentSnapshot doc : snapshot.getDocuments()) {
                    //Extraemos los datos del mensaje
                    String texto = doc.getString("text");
                    String sender = doc.getString("sender");
                    //Comprobacion para saber si es usuario o IA
                    boolean es_usuario;
                    if(sender.equalsIgnoreCase("user")){
                        es_usuario=true;
                    }
                    else{
                        es_usuario=false;
                    }
                    //Escribimos el contenido del mensaje recuperado en la base de datos Firestore, en segundo plano para no bloquear la ui
                    Platform.runLater(() -> {
                        System.out.println("llego hasta aqui");
                        addMessageBox(texto,es_usuario);
                    });
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }).start();

    }

    /***
     * guardar mensajes en firestore
     * @param message
     * @param sender
     */
    private void saveMessage(String message, String sender) {
        //guardamos en la variable bd la instancia de la base de datos
        Firestore db = FirebaseService.getDb();
        //guradamos el localid del usuario actual
        String uid = Sesion.getUserId();
        //Creamos como estructura de datos un Mao para guardar el mensaje, quien lo envia y la fecha en que se envia(representacion del mensaje)
        Map<String, Object> msg = new HashMap<>();
        msg.put("text", message);
        msg.put("sender", sender);
        msg.put("timestamp", System.currentTimeMillis());
        //Persistimos en la base de datos de firestore el map que va a contener toda la informacion del mensaje
        //a la coleccion users -> la instancia que tenga document (id) -> en la coleccion chats -> que tenga document = chatId
        //-> en la coleccion messages, persistimos el mensaje
        db.collection("users")
                .document(uid)
                .collection("chats")
                .document(chatId)
                .collection("messages")
                .add(msg);

    }

    /***
     * metodo para crear un nuvo chat en firestore si no existe chatID
     */
    private void crearChat(){
        //Si existe chat -> nada
        if(chatId != null) return;
        Firestore db = FirebaseService.getDb();//instancia db
        String uid = Sesion.getUserId();//localid user actual

        //Creamos un documento vacio dentro de chats--> sirve para generar automaticamente el chatID para el nuevo chat
        DocumentReference doc = db.collection("users").document(uid).collection("chats").document();
        chatId = doc.getId();//asiganmos el id generado al caht actual
        //informacion del nuevo chat
        Map<String, Object> chat  = new HashMap<>();
        chat.put("title", "Nueva conversacion");
        chat.put("createdAt", System.currentTimeMillis());
        //persistimo la informacion en firestore
        doc.set(chat);
    }

    //Este metodo serviria para controlar los mensajes si contienen algo que no deben o podemos hacerlo diciendoselo a la IA que lo controle,
    //pero con el metodo podemos mostrar un mensaje al user si eso ocurre o incluso si lo hace varias veces hacer algo distitno.
    private void detectSensitiveContent(String message) {
        boolean isSensitive = message.contains("ESO");
    }

    //Metodo para probar quer funciona correctamente
    public void testUI() {
        addMessageBox("Hola, soy un mensaje del usuario", true);
        addMessageBox("Hola, soy un mensaje del bot aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", false);
        addMessageBox("Este es un mensaje largo para comprobar que el wrap funciona correctamente y que la burbuja se adapta al tamaño del texto sin romper el diseño.", true);
        addMessageBox("Perfecto, funciona genial", false);
    }

    //Efecto para mostrar mensajes en el chat poco a poco, solo es para los mensajes de la IA.
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

    //Metodo que se ejecuta al iniciar la pantalla del controller, es decir la de Chat en este caso.
    @FXML
    public void initialize() {
        //Hace que el autoScroll funcione correctamente, detecta cada vez que el VBox cambia de altura y baja el scroll del todo.
        messagesBox.heightProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> scrollPane.setVvalue(1.0));
        });

        //La APIKey habra que cambiarlo lo mas seguro, se queda sin tokens.
        chatService = new ChatService(new IAServer("AIzaSyAiCXmfgyJz0ujR4UoZBR0kMNitmmrS5Mo"));

        //testUI();
    }
}
