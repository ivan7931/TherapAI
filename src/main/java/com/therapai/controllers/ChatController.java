package com.therapai.controllers;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.therapai.components.TypingBubble;
import com.therapai.servicies.ChatService;
import com.therapai.servicies.FirebaseService;
import com.therapai.servicies.IAServer;
import com.therapai.utils.SceneManager;
import com.therapai.utils.Sesion;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
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
    //variable para controlar uqe se la ia ha generado un titulo
    private boolean tituloGenerado = false;
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
     * metodo que coge los 15 primeros caracteres del mensaje del usuario
     * y los asigna como titulo al chat en Firestore
     */
    private void generarTituloFallback(String message) {
        String titulo;
        if (message.length() > 15) {
            titulo = message.substring(0, 15) + "[...]";
        } else {
            titulo = message;
        }

        updateTitle(titulo);
    }
    /***
     * Enviar mensaje y obtener respuesta de la IA
     */
    @FXML
    public void sendMessage() {
        // Recuperamos el texto que ha escrito el usuario -> si vacío nada
        String message = messageField1.getText().trim();
        if (message.isEmpty()) return;

        // Si es la primera vez que se envía un mensaje -> creamos nuevo chat en Firestore
        crearChat();


        // Se muestra el mensaje del usuario en la UI
        addMessageBox(message, true);

        // Guardamos el mensaje que envía el usuario en Firestore
        saveMessage(message, "user");

        //si no se genera titulo con la ia
        if (!tituloGenerado) {
            generarTituloFallback(message);
            tituloGenerado = true;
        }

        // Limpiamos campo
        messageField1.clear();

        // Se muestra la animacion de los 3 puntos mientras que la IA piensa la respuesta
        TypingBubble typingBubble = new TypingBubble();

        Platform.runLater(() -> {
            messagesBox.getChildren().add(typingBubble);
        });
        // Creamos una Task para que la UI no se bloquee mientras la IA piensa//
        /**
         * task clase javafx oara ejecutar cosas pesadas en segundo plano sin congelar la ui
         * */
        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return chatService.sendMessageToAI(message);
            }
        };

        //Cuando la IA responde(task -> temina ok)
        task.setOnSucceeded(event -> {
            String aiResponse = task.getValue();

            // Quitar animación
            messagesBox.getChildren().remove(typingBubble);
            typingBubble.stop();

            if (aiResponse == null ||
                    aiResponse.contains("Error") ||
                    aiResponse.contains("ERROR") ||
                    aiResponse.contains("error")) {

                addTypingMessage("El servicio de IA no está disponible.");
                saveMessage("servicio de IA no disponible", "IA");
                //generamos el titulo antes de retornar si la ia no dispnible
                return;
            }

            aiResponse = aiResponse.replace("\n", System.lineSeparator());

            // Mostrar respuesta con efecto typing
            addTypingMessage(aiResponse);

            // Guardar mensaje de la IA
            saveMessage(aiResponse, "IA");
            if (esPrimerMensajeDelChat()) {
                generarTituloAutomatico(message);
            }

        });

        // Si falla la llamada
        task.setOnFailed(event -> {
            messagesBox.getChildren().remove(typingBubble);
            typingBubble.stop();

            addTypingMessage("Error al conectar con la IA.");
            saveMessage("Error al conectar con la IA.", "IA");
        });

        //Ejecutamos el task en un hilo en segundo plano para que no se bloquee la UI
        new Thread(task).start();
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
    //Metodo para comprobar si es el primer mensaje del chat
    private boolean esPrimerMensajeDelChat() {
        Firestore db = FirebaseService.getDb();
        String uid = Sesion.getUserId();

        try {
            ApiFuture<QuerySnapshot> future = db.collection("users")
                    .document(uid)
                    .collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .get();

            // Si solo hay 1 mensaje es el del usuario por lo tanto es el primer mensaje
            return future.get().size() == 1;
        } catch (Exception e) {
            return false;
        }
    }

    //Metodo para generar el titulo del chat automaticamente
    private void generarTituloAutomatico(String primerMensaje) {

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                String prompt =
                        "Genera un título muy corto (máximo 4 palabras), neutro, profesional y sin emociones " +
                                "para resumir este mensaje. No incluyas detalles personales, no incluyas emociones, " +
                                "no incluyas nombres propios. Solo devuelve el título:\n\n" + primerMensaje;
                return chatService.sendMessageToAI(prompt);
            }
        };

        task.setOnSucceeded(e -> {
            String titulo = task.getValue();
            // Si la ia no puede generar mensaje se coge como titulo parte del primer mensaje del usuario
            if (titulo == null || titulo.isBlank() || titulo.toLowerCase().contains("error")){
                generarTituloFallback(primerMensaje);
                return;
            }

            titulo = titulo.replace("\n", "").trim();
            updateTitle(titulo);

            //en caso de fallar la tarea de generar el titulo del chat se guarda el contenido del primer mensaje como
            //titulo
            task.setOnFailed(event->{
                generarTituloFallback(primerMensaje);
            });
        });
        new Thread(task).start();
    }

    /***
     * metodo para actualizar el titulo en FIrestore en el documento correspondiete
     * pensado para evitar duplicar codigo
     * @param titulo
     */
    private void updateTitle(String titulo) {

        Firestore db = FirebaseService.getDb();
        String uid = Sesion.getUserId();

        db.collection("users")
                .document(uid)
                .collection("chats")
                .document(chatId)
                .update("title", titulo);
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
        //chat.put("title", "Nueva conversacion");
        chat.put("title","");
        chat.put("createdAt", System.currentTimeMillis());
        //persistimo la informacion en firestore
        doc.set(chat);
    }

    //Este metodo serviria para controlar los mensajes si contienen algo que no deben o podemos hacerlo diciendoselo a la IA que lo controle,
    //pero con el metodo podemos mostrar un mensaje al user si eso ocurre o incluso si lo hace varias veces hacer algo distitno.
    private void detectSensitiveContent(String message) {
        boolean isSensitive = message.contains("ESO");
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
        chatService = new ChatService(new IAServer("AIzaSyDYctgaQMOy-zwNaQuUayejPsIfpINMgRQ"));

        //testUI();
    }
}
