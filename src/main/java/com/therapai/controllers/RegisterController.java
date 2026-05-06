package com.therapai.controllers;

import com.google.cloud.firestore.Firestore;
import com.therapai.models.UserModel;
import com.therapai.servicies.FirebaseService;
import com.therapai.utils.SceneManager;
import com.therapai.utils.Sesion;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class RegisterController {


    //firebase login/register api key-> necesaria para autenticar la peticion
    private final String API_KEY_FIREBASE = "AIzaSyAWyyMF2sEyucztZAZU9FQOCL_YeQnyMXY";

    //variables para guardar los datos introducidos por el usuario en la aplicaicon
    @FXML private TextField fullName;
    @FXML private TextField registerEmail;
    @FXML private PasswordField registerPassword;
    @FXML private PasswordField registerConfirmPass;
    @FXML private Button confirmButton;

    /***
     * funcion para cambiar a home si registro correcto
     */
    @FXML
    public void goHome() {
        SceneManager.getInstance().switchTo("home.fxml");
    }

    @FXML
    public void register() {
        //cogemos los textos de los campos de la ventana Registro
        String name =  fullName.getText();
        String email = registerEmail.getText();
        String password = registerPassword.getText();
        String confirmPassword = registerConfirmPass.getText();

        //Comprobar si existe algun campo vacio -> enviamos mensaje al usuario hasta que todos los campos completos
        if(name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Campos vacios ");
            alert.setHeaderText(null);
            alert.setContentText("Por favor rellene todos los campos para completar el registro");
            alert.showAndWait();
            return;
        }
        //validacion de contraseñas-> si distintas resetear contraseñas+ mensaje  informativo
        if(!password.equals(confirmPassword)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Contraseñas no coinciden");
            alert.setHeaderText(null);
            alert.setContentText("Las contraseñas deben ser iguales");
            alert.showAndWait();
            registerPassword.setText("");
            registerConfirmPass.setText("");
            return;
        }
        //Creamos el cliente http que se va a encargar de realizar la petidcion
        OkHttpClient client = new OkHttpClient();
        //ur -> contiene la url de la api firebase junto con neustra api key
        String url = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + API_KEY_FIREBASE;
        //crear json que enviamos en la peticion
        JSONObject objPeticion = new JSONObject();
        objPeticion.put("email", email);
        objPeticion.put("password", password);
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
                    return;

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
                       //si registro -> ok
                       if(response.isSuccessful()){
                           //obtenemos los datos devueltos por firebase
                           String uid = objRespuesta.getString("localId");
                           String emailRespuesta = objRespuesta.getString("email");
                           System.out.println("registro correcto->"+res);
                           UserModel userOK = new UserModel(uid, emailRespuesta,name);
                           //Obtenemos la instancia de Firestore
                           Firestore db = FirebaseService.getDb();
                           //Hemos optado por usar un map como que represente el documento
                           //en firestore. nombre-email = documento para la coleccion de users
                           Map<String,Object> userPersist = new HashMap<>();
                           //almacenamos los datos del usuario en el documento
                           userPersist.put("name",name);
                           userPersist.put("email",emailRespuesta);
                           //Persistimos el documento(nombre-email) en firestore en la coleccion users
                           //el documento se identifica por el localid del usuario (unuico para cada usuario)
                           db.collection("users").document(uid).set(userPersist);
                           //Guardamos la informacion de la sesion actual y cambiamos a home
                           Sesion.setUsuario_actual(userOK);
                           goHome();
                       }
                       else {
                           //firebase devuelve el error en formato json
                           JSONObject error = objRespuesta.getJSONObject("error");
                           String mensaje = error.getString("message");
                           Alert alerta = new Alert(Alert.AlertType.ERROR);
                           alerta.setTitle("Error");
                           alerta.setHeaderText(null);
                           alerta.setContentText("No se pudo realizar el registro :"+mensaje);
                           alerta.showAndWait();
                           fullName.clear();
                           registerEmail.clear();
                           registerPassword.clear();
                           registerConfirmPass.clear();
                           return;

                       }
                   } catch (Exception e) {
                       e.printStackTrace();
                   }
                });

            }
        });



    }

    @FXML
    public void initialize() {
        confirmButton.setDefaultButton(true);
    }
    @FXML
    public void goLogin(){
        SceneManager.getInstance().switchTo("login.fxml");
    }
}
