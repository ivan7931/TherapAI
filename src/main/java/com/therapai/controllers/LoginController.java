package com.therapai.controllers;

import com.therapai.utils.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    private final String API_KEY_FIREBASE = "AIzaSyAWyyMF2sEyucztZAZU9FQOCL_YeQnyMXY";
    @FXML
    public void login() {
        //Texto que el usuario ha introducido en los campos email/password
        String email = emailField.getText();
        String password = passwordField.getText();
        //Si algun campo vacio -> mostrar mensaje + reset password
        if (email.isEmpty() || password.isEmpty()) {
            emailField.setText("Email or password is empty");
            passwordField.setText("");
            return;
        }
        //Cliente HTTP en java para crear la peticion http y recibir la respuesta JSON
        OkHttpClient cliente = new OkHttpClient();
        /*
        url con la Api de Firebase Authentication
        Envia email+password a Firebase --> Firebase responde para saber si existe ususario y contraseña ok
        url_login--> definicion del endpoint de firebase + api key para identificar proyecto
        * */
        String url_login = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key="+API_KEY_FIREBASE;
        /*
        Json que enviamos a firebase
         */
        JSONObject json = new JSONObject();
        json.put("email", email);
        json.put("password", password);
        //returnsecuretoken --> si login correcto la llamada a la api devuelve los tokens de login
        json.put("returnSecureToken",true);
        //preparamos el json en cuerpo http
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), json.toString());
        //Contruimos la peticion http. Definimos url destin, metodo post y datos a enviar
        Request request = new Request.Builder().url(url_login).post(body).build();
        //Enciamos la peticion de manera asincrona , para ejecutar en segundo plano y sin bloquear app
        cliente.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                javafx.application.Platform.runLater(() ->
                        emailField.setText("Error de conexión")
                );
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String res = response.body().string();

                javafx.application.Platform.runLater(() -> {

                    if (response.isSuccessful()) {
                        //errorLabel.setText("Login correcto ✔");

                        // aquí luego cambias a Home.fxml
                        System.out.println("LOGIN OK: " + res);

                    } else {
                        emailField.setText("usuario/contraseña incorrecto");
                        passwordField.setText("");
                    }
                });
            }
        });




    }

    @FXML
    public void goRegister() {
        SceneManager.getInstance().switchTo("register.fxml");
    }
}
