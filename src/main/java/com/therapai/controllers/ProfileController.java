package com.therapai.controllers;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.therapai.models.UserModel;
import com.therapai.servicies.FirebaseService;
import com.therapai.utils.SceneManager;
import com.therapai.utils.Sesion;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;


public class ProfileController {
    //api key firebase
    private final String API_KEY_FIREBASE = "AIzaSyAWyyMF2sEyucztZAZU9FQOCL_YeQnyMXY";
    @FXML
    private Button logoutButton;
    @FXML
    private Label nameLabel;

    @FXML
    private Label emailLabel;

    private void loadUserData() {
        try {
            UserModel usuario = Sesion.getUsuario_actual();

            if (usuario == null) {
                System.out.println("No hay usuario en sesión");
                return;
            }

            nameLabel.setText(usuario.getName());
            emailLabel.setText(usuario.getEmail());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void logOut(){
        //Borramos de memoria el usuario actual
        Sesion.setUsuario_actual(null);
        //Regresamos a la ventana de login
        //No tenemos que hacer llamadas a firestore porque no usamos firesotre
        //para el tema del login. Con firestore simplemente validamos que el
        //usario esté registrado--> Si registrado = dejamos que use app
        SceneManager.getInstance().switchTo("login.fxml");
    }

    @FXML
    public void goHome(MouseEvent mouseEvent) {
        SceneManager.getInstance().switchTo("home.fxml");
    }

    private void abrirPopupFormulario(String titulo, String valorInicial, Consumer<String[]> onConfirm, boolean mostrarConfirmacion) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/therapai/views/popup_form.fxml"));
            Parent root = loader.load();

            PopupFormController controller = loader.getController();
            controller.titleLabel.setText(titulo);
            controller.field1.setText(valorInicial);

            // Aquí activamos o desactivamos el segundo campo
            controller.enableConfirmField(mostrarConfirmacion);

            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);

            stage.setScene(scene);
            stage.show();

            controller.setOnConfirm(onConfirm);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void abrirPopupConfirmacion(String titulo, String mensaje, Runnable onConfirm) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/therapai/views/popup_confirm.fxml"));
            Parent root = loader.load();

            PopupConfirmController controller = loader.getController();
            controller.titleLabel.setText(titulo);
            controller.messageLabel.setText(mensaje);

            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);

            stage.setScene(scene);
            stage.show();

            controller.setOnConfirm(onConfirm);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void editName() {
        abrirPopupFormulario("Cambiar nombre", Sesion.getUsuario_actual().getName(), valores -> {
            String nuevoNombre = valores[0];

            if (nuevoNombre == null || nuevoNombre.isBlank()){
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Nombre incorrecto");
                alert.setHeaderText(null);
                alert.setContentText("El nombre no es un parametro válido");
                alert.showAndWait();
                return;
            }

            // Actualizar sesión
            Sesion.getUsuario_actual().setName(nuevoNombre);

            // Actualizar UI
            nameLabel.setText(nuevoNombre);

            // Actualizar Firestore
            FirebaseService.getDb().collection("users")
                    .document(Sesion.getUserId())
                    .update("name", nuevoNombre);
        }, false);
    }

    @FXML
    private void changePassword() {
        abrirPopupFormulario("Cambiar contraseña", "", valores -> {
            String pass1 = valores[0];
            String pass2 = valores[1];

            if (pass1 == null || pass1.isBlank()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Contraseña vacia");
                alert.setHeaderText(null);
                alert.setContentText("La contraseña no puede estar vacia");
                alert.showAndWait();
                return;
            }
            if (!pass1.equals(pass2)) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Contraseñas distintas");
                alert.setHeaderText(null);
                alert.setContentText("La contraseñas no coinciden");
                alert.showAndWait();
                return;
            }

            /*
            FirebaseService.getDb().collection("users")
                    .document(Sesion.getUserId())
                    .update("password", pass1);*/
            //Creamos el cliente http que se va a encargar de realizar la petidcion
            OkHttpClient client = new OkHttpClient();
            //ur -> contiene la url de la api firebase junto con neustra api key
            String url = "https://identitytoolkit.googleapis.com/v1/accounts:update?key=" + API_KEY_FIREBASE;
            //crear json que enviamos en la peticion
            JSONObject objPeticion = new JSONObject();
            objPeticion.put("idToken", Sesion.getUsuario_actual().getIdToken());
            objPeticion.put("password", pass1);
            objPeticion.put("returnSecureToken", true);

            //Convertimos el json a requestbody -> formato aplicacion /json
            RequestBody body = RequestBody.create(MediaType.parse("application/json"), objPeticion.toString());
            //Construimos la peticion htto para realizar el post
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            //Usamos el metodo enqueue para ejecutar la peticion en segundo plano y de forma asincrona
            //para no bloquear la aplicacion
            client.newCall(request).enqueue(new Callback() {
                //Codigo en fallo de conexion
                @Override
                public void onFailure(Call call, IOException e) {
                    //volvemos al hilo prnicipal para informar al usuario de error en la conexion
                    javafx.application.Platform.runLater(() -> {
                        Alert alerta = new Alert(Alert.AlertType.ERROR);
                        alerta.setTitle("Error");
                        alerta.setHeaderText(null);
                        alerta.setContentText("Ocurrio un error en la conexion");
                        alerta.showAndWait();

                    });
                }

                //Respuesta del servidor-> con registro correcto / fallo en el registro
                @Override
                public void onResponse(Call call,Response response) throws IOException {
                    //Obtenemos el cuerpo de respuesta(json) como string
                    String res = response.body().string();
                    //Para ver el cuerpo del json reicbido
                    System.out.println("JSON RECIBIDO: " + res);
                    //Volvemos al hilo principal para ejecutar cambios necesarios en la aplicacion
                    javafx.application.Platform.runLater(() -> {
                        try{
                            //pasamos la respuesta obtenida como string -> objRespuesta json
                            JSONObject objRespuesta = new JSONObject(res);
                            //si cambio de contraseña  -> ok
                            if(response.isSuccessful()){
                                //firebase devuelve un nuevo token . Actualizamos los datos de la sesion acutal
                                String nuevoIdToken = objRespuesta.getString("idToken");
                                Sesion.getUsuario_actual().setIdToken(nuevoIdToken);
                                //Informamos al usuario por medio de un alaerta de que se ha actualizado su contraseña
                                Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                                alerta.setTitle("Cambio correcto");
                                alerta.setHeaderText(null);
                                alerta.setContentText("Se actualizo la contraseña");
                                alerta.showAndWait();
                            }
                            else {
                                //firebase devuelve el error en formato json
                                JSONObject error = objRespuesta.getJSONObject("error");
                                //CAPTURAMOS EL mensaje de rror
                                String mensaje = error.getString("message");
                                Alert alerta = new Alert(Alert.AlertType.ERROR);
                                alerta.setTitle("Error");
                                alerta.setHeaderText(null);
                                alerta.setContentText("No se pudo cambiar la contraseña :"+mensaje);
                                alerta.showAndWait();

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                }
            });
        }, true);
    }

    //para que funcione el metodo de cambio de email en la consola de firestore
    //quitamos la opcion de verificar email(que firebase envio un correo a esa cuenta y el usuario lo acepto el cambio)
    //podemos añadir las siguientes lineas de codigo para hardcodear esta limitacion si no lo cambiamos en firebase console
    /*String mensaje = error.getString("message");
    if(mensaje.contains("OPERATION_NOT_ALLOWED")){
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Acción no permitida");
        alerta.setHeaderText(null);
        alerta.setContentText(
                "Debes verificar el nuevo email antes de cambiarlo"
        );
        alerta.showAndWait();

        return;
    }*/

    @FXML
    private void changeEmail() {
        abrirPopupFormulario("Cambiar email", Sesion.getUsuario_actual().getEmail(), valores -> {
            String nuevoEmail = valores[0];

            if (nuevoEmail == null || nuevoEmail.isBlank()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Email vacia");
                alert.setHeaderText(null);
                alert.setContentText("El email no puede estar vacio");
                alert.showAndWait();
                return;
            }

            // Actualizar sesión
            Sesion.getUsuario_actual().setEmail(nuevoEmail);

            // Actualizar UI
            emailLabel.setText(nuevoEmail);

            // Actualizar Firestore
            /*FirebaseService.getDb().collection("users")
                    .document(Sesion.getUserId())
                    .update("email", nuevoEmail);*/
            //Creamos el cliente http que se va a encargar de realizar la petidcion
            OkHttpClient client = new OkHttpClient();
            //ur -> contiene la url de la api firebase junto con neustra api key
            String url = "https://identitytoolkit.googleapis.com/v1/accounts:update?key=" + API_KEY_FIREBASE;
            //crear json que enviamos en la peticion
            JSONObject objPeticion = new JSONObject();
            objPeticion.put("idToken", Sesion.getUsuario_actual().getIdToken());
            objPeticion.put("email", nuevoEmail);
            objPeticion.put("returnSecureToken", true);

            //Convertimos el json a requestbody -> formato aplicacion /json
            RequestBody body = RequestBody.create(MediaType.parse("application/json"), objPeticion.toString());
            //Construimos la peticion htto para realizar el post
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            //Usamos el metodo enqueue para ejecutar la peticion en segundo plano y de forma asincrona
            //para no bloquear la aplicacion
            client.newCall(request).enqueue(new Callback() {
                //Codigo en fallo de conexion
                @Override
                public void onFailure(Call call, IOException e) {
                    //volvemos al hilo prnicipal para informar al usuario de error en la conexion
                    javafx.application.Platform.runLater(() -> {
                        Alert alerta = new Alert(Alert.AlertType.ERROR);
                        alerta.setTitle("Error");
                        alerta.setHeaderText(null);
                        alerta.setContentText("Ocurrio un error en la conexion");
                        alerta.showAndWait();

                    });
                }

                //Respuesta del servidor-> con registro correcto / fallo en el registro
                @Override
                public void onResponse(Call call,Response response) throws IOException {
                    //Obtenemos el cuerpo de respuesta(json) como string
                    String res = response.body().string();
                    //Para ver el cuerpo del json reicbido
                    System.out.println("JSON RECIBIDO: " + res);
                    //Volvemos al hilo principal para ejecutar cambios necesarios en la aplicacion
                    javafx.application.Platform.runLater(() -> {
                        try{
                            //pasamos la respuesta obtenida como string -> objRespuesta json
                            JSONObject objRespuesta = new JSONObject(res);
                            //si cambio de email  -> ok
                            if(response.isSuccessful()){
                                //firebase devuelve un nuevo token . Actualizamos los datos de la sesion acutal
                                String nuevoIdToken = objRespuesta.getString("idToken");
                                Sesion.getUsuario_actual().setIdToken(nuevoIdToken);
                                //Actualizamos el email en la sesion actual
                                Sesion.getUsuario_actual().setEmail(nuevoEmail);
                                //Actualizamos los datos en firestre. El codigo anterior era para cambair el email
                                // en firebase auth. COn lo siguiente persistimos los datos en firetstore
                                Firestore db = FirebaseService.getDb();
                                db.collection("users")
                                        .document(Sesion.getUserId())
                                        .update("email", nuevoEmail);//pERSISTIMOS EL CAMBIO
                                emailLabel.setText(nuevoEmail);//actualizamos ui
                                //Informamos al usuario por medio de un alaerta de que se ha actualizado su contraseña
                                Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                                alerta.setTitle("Cambio correcto");
                                alerta.setHeaderText(null);
                                alerta.setContentText("Se actualizo el email");
                                alerta.showAndWait();
                            }
                            else {
                                //firebase devuelve el error en formato json
                                JSONObject error = objRespuesta.getJSONObject("error");
                                //CAPTURAMOS EL mensaje de rror
                                String mensaje = error.getString("message");
                                if (mensaje.contains("OPERATION_NOT_ALLOWED")) {

                                    Alert alerta = new Alert(Alert.AlertType.WARNING);
                                    alerta.setTitle("Acción no permitida");
                                    alerta.setHeaderText(null);
                                    alerta.setContentText(
                                            "Debes verificar el nuevo email antes de cambiarlo"
                                    );
                                    alerta.showAndWait();

                                    return;
                                }
                                Alert alerta = new Alert(Alert.AlertType.ERROR);
                                alerta.setTitle("Error");
                                alerta.setHeaderText(null);
                                alerta.setContentText("No se pudo cambiar el email :"+mensaje);
                                alerta.showAndWait();

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                }
            });
        }, false);
    }

    @FXML
    private void manageSubscription() {}

    /****
     * Metodo auxiliar para eliminar la coleccion chas del user y poder hacer el eliminado del usuario en firestore
     *
     */
    private void eliminarChatFirestore(String chatId) {

        Firestore db = FirebaseService.getDb();
        String userId = Sesion.getUserId();

        try {

            // Obtener mensajes
            ApiFuture<QuerySnapshot> future =
                    db.collection("users")
                            .document(userId)
                            .collection("chats")
                            .document(chatId)
                            .collection("messages")
                            .get();

            QuerySnapshot messages = future.get();

            // Borrar mensajes
            for (DocumentSnapshot msg : messages.getDocuments()) {
                db.collection("users")
                        .document(userId)
                        .collection("chats")
                        .document(chatId)
                        .collection("messages")
                        .document(msg.getId())
                        .delete();
            }

            // Borrar chat
            db.collection("users")
                    .document(userId)
                    .collection("chats")
                    .document(chatId)
                    .delete();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /****
     *
     * metodo auxiliar para borrar la cuenta de firebase auth
     *
     */
    private void borrarCuentaAuth() {

        try {

            OkHttpClient client = new OkHttpClient();
            //url api para borrar cuenta de firebase auth
            String url = "https://identitytoolkit.googleapis.com/v1/accounts:delete?key=" + API_KEY_FIREBASE;
            JSONObject json = new JSONObject();
            json.put(
                    "idToken",
                    Sesion.getUsuario_actual().getIdToken()
            );

            RequestBody body = RequestBody.create(MediaType.parse("application/json"), json.toString());

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();
            String res = response.body().string();
            System.out.println("json devuetlo: " + res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void deleteData() {
        abrirPopupConfirmacion(
                "Borrar datos",
                "¿Estás seguro de que quieres borrar todos tus datos? Esta acción no se puede deshacer.",
                () -> {
                    Firestore db = FirebaseService.getDb();
                    String userId = Sesion.getUserId();
                    new Thread(() -> {
                        try {
                            //obtenemos todos los chats del usuario
                            ApiFuture<QuerySnapshot> future = db.collection("users")
                                    .document(userId)
                                    .collection("chats")
                                    .get();
                            QuerySnapshot chatSnapShot = future.get();
                            //Borramos cada chat usando el metodo auxiliar eliminarChatFirestore(String chatId)
                            for (DocumentSnapshot chat : chatSnapShot.getDocuments()) {
                                eliminarChatFirestore(chat.getId());
                            }
                            //eliminamos el usuario identificado por userID de firestore
                            db.collection("users")
                                    .document(userId)
                                    .delete();
                            //eliminmamos la cuenta de firebase auth
                            borrarCuentaAuth();
                            //al estar en un hilo secundario  todos los cambios en la ui que hagamos deben hacerse
                            //desde el hilo principal , por lo que volvemos al hilo principal
                            //de javafx usando Plantofrom.runlater
                            Platform.runLater(() -> {
                            //infomramos al usuario de que se borro su ceunta
                            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                            alerta.setTitle("Cuenta eliminda");
                            alerta.setHeaderText(null);
                            alerta.setContentText("Se borro la cuenta");
                            alerta.showAndWait();
                            Sesion.setUsuario_actual(null);
                            //cambiamos al login
                            SceneManager.getInstance().switchTo("login.fxml");
                            });
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }

                    }).start();
                    /*
                    FirebaseService.getDb().collection("users")
                            .document(Sesion.getUserId())
                            .delete();

                    // Cerrar sesión
                    Sesion.setUsuario_actual(null);

                    // Volver al login
                    SceneManager.getInstance().switchTo("login.fxml");*/
                }
        );
    }

    @FXML
    private void contactUs() {}

    @FXML
    public void initialize() {
        loadUserData();
    }
}
