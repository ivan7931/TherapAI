package com.therapai.controllers;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.therapai.components.ChatCell;
import com.therapai.models.ConversationModel;
import com.therapai.servicies.FirebaseService;
import com.therapai.utils.SceneManager;
import com.therapai.utils.Sesion;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class HomeController {
    //lista de chats del usuario logeado
    @FXML
    private ListView<ConversationModel> chatList;

    /***
     * Metodo para recuperar la  lsita de cahts del usuario de firestore
     */
    public void cargarChats(){
        //Obtenemos instancia de firestore
        Firestore db = FirebaseService.getDb();
        //recuperamos localid del usuario actual
        String userId = Sesion.getUserId();
        //Accedemos a la coleccion chats del usuario
        /*db.collection("users")
                .document(userId)
                .collection("chats")
                //ordenamos por fecha de creacion
                .orderBy("createdAt", Query.Direction.DESCENDING)
                //Recuperamos los datos
                .get()
                //Guardamos de manera asincrona para no bloquear la UI con la funcion lambda
                .addOnSuccesListener(querySnapshot ->{});*/
        //Accedemos a la coleccion de chats del usuario de la manera indicada para admin sdk
        ApiFuture<QuerySnapshot> future = db.collection("users")
                .document(userId)
                .collection("chats")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
        //recuperamos los datos de manera asincrona y en segundo plano para no bloquear la ui
        new Thread(()->{
            try {
                QuerySnapshot querySnapshot = future.get();

                //Lista donde guardamos los chats
                List<ConversationModel> chats = new ArrayList<>();

                //Recorremos cada documento que se encuentra en la coleccion chats(un documento = un chat)
                for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                    //Hacemos la conversion de objeto Firestore a java
                    String chatId = documentSnapshot.getId();
                    String titulo = documentSnapshot.getString("title");
                    Long cratedAt = documentSnapshot.getLong("createdAt");
                    //Si titulo == null-> nombre por defecto = Chat sin titulo
                    if (titulo==null){
                        titulo = "Chat sin titulo";
                    }

                    //Creamos el modelo de chat con los datos recuperados de firestores y los añadimos a la lista
                    chats.add(new ConversationModel(userId,chatId,cratedAt,titulo));
                }
                Platform.runLater(()->{
                    chatList.getItems().setAll(chats);
                });
            }
            //capturamos error
            catch(Exception e){
                e.printStackTrace();
            }
        }).start();


    }
    @FXML
    public void newChat() {
        SceneManager.getInstance().switchTo("chat.fxml");
    }

    //Metodo para abrir la conversacion que selecciono de la lista.
    @FXML
    public void openChat() {
        ConversationModel selected = chatList.getSelectionModel().getSelectedItem();

        if (selected == null) {
            System.out.println("⚠ No hay chat seleccionado");
            return;
        }

        String chatId = selected.getChatId();

        //Con el getInstance se obtiene el SceneManager que es general para toda la app,
        // el metodo switchToWithController nos cambia a la pantalla que pasamos y nos devuelve el controller de esa scene y
        //asi podemos pasar parametros entre pantallas.
        ChatController chatController = SceneManager.getInstance().switchToWithController("chat.fxml");

        chatController.loadChat(chatId);
    }

    //Metodo para el boton de eliminar el chat
    private void eliminarChat(ConversationModel chat) {
        //instancia de la base de datos
        Firestore db = FirebaseService.getDb();
        //obtenemos el localId del usuario acctual
        String userId = Sesion.getUserId();
        //ejecutamos en segundo planopara no bloquear la ui
        new Thread(()->{
            try{
                //recuperamos los mensajes del chat recorriendo las colecciones hasta llegar a mensajes
                ApiFuture<QuerySnapshot> future = db.collection("users")
                        .document(userId)
                        .collection("chats")
                        .document(chat.getChatId())
                        .collection("messages")
                        .get();
                QuerySnapshot querySnapshot = future.get();

                //Borramos cada uno de los mensajes del chat elegido
                for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                    db.collection("users")
                            .document(userId)
                            .collection("chats")
                            .document(chat.getChatId())
                            .collection("messages")
                            .document(documentSnapshot.getId())
                            .delete();
                }
                //Una vez borrados todos los mensajes del chat borramos el documento del chat actual de Firestore
                //Hacemos esto porque cuando eliminaos un documento en firestore no se elimnian los hijos en
                //cascada y los mensajes quedarian oersistidos en firestore pero huerfanos y el borrado del cha tno se ejecutaria
                db.collection("users")
                        .document(userId)
                        .collection("chats")
                        .document(chat.getChatId())
                        .delete();

                //Actualizamos la ui
                Platform.runLater(()->{
                    chatList.getItems().remove(chat);
                });
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    //Metodo para abrir el chat desde el Cell pulsando el chat
    private void openChatFromCell(ConversationModel chat) {
        ChatController controller = SceneManager.getInstance().switchToWithController("chat.fxml");
        controller.loadChat(chat.getChatId());
    }

    /***
     * Metodo para cargar la lisa de chats del usuario cuando se carga la pantalla home.fxml
     */
    @FXML
    public void initialize() {
        System.out.println("HomeController initialized");
        // 1. Si Firebase no está listo, evitamos NPE
        if (FirebaseService.getDb() == null) {
            System.err.println("⚠ Firebase no está inicializado todavía.");
            return;
        }
        chatList.setStyle("-fx-background-color: #0F172A; -fx-control-inner-background: #0F172A;");

        //Aplicar CellFactory personalizado
        chatList.setCellFactory(list -> new ChatCell(
                chat -> eliminarChat(chat),      // botón de borrar
                chat -> openChatFromCell(chat)   // abrir chat al hacer clic
        ));

        //Cargar los chats del usuario
        cargarChats();
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
