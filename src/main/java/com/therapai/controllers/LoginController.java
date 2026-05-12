package com.therapai.controllers;

import com.therapai.models.UserModel;
import com.therapai.utils.SceneManager;
import com.therapai.utils.Sesion;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;


public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    //Codigo para que detecte el enter en el login y no hacer clcik para registrarse
    @FXML private Button loginButton;

    @FXML
    public void goHome() {
        SceneManager.getInstance().switchTo("home.fxml");
    }
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
        //Enviamos la peticion para ejecutar en segundo plano y sin bloquear app-->Cliente.newCall(request)=crea peticion
        //.enqueue()=ejecuta en seguno plano
        //callback maneja exito error, interfaz de OkHttp
        //creamos una clase anonima que implementa la interfaz Callback
        //.enqueue--> necesita obligatoriamente Callback para saber que hacer si falla conexion(onFailure) y que hacer si responde(onResponse)
        cliente.newCall(request).enqueue(new Callback() {
            /*
             * Metodos de la interfaz Callback onFailure, onResponse()
             * */

            /***
             *
             * Si conxecion falla --> informa en el campo email
             * Platfor.runlater vuelve al hilo principal(JAVAFX SOLO PERMITE MODFICAR LA UI DESDE HILO PRINCIPAL)
             * Si conexion falla--> reset email/password y mensaje en email
             */
            //Se usan funciones lambda para tener que escribir:
            /*
            public void onFailure(Call call, IOException e) {
            Platform.runLater(new Runnable() {
               @Override
                public void run() {
                    emailField.setText("Error de conexión");
                }
              });
            }

             */
            @Override
            public void onFailure(Call call, IOException e) {
                javafx.application.Platform.runLater(() -> {
                            emailField.setText("Error de conexión");
                            passwordField.setText("");
                        }
                );
            }

            /***
             * El servidor responde ok
             * Devuelve json con respuesta del servidor Firebase si login correcto || login falla
             * @param call
             * @param response
             * @throws IOException
             */
            //Se usan lambdas para evitar:
            /*
            public void onResponse(Call call, Response response) throws IOException {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccessful()) {
                         System.out.println("LOGIN OK: " + res);
                        } else {
                            emailField.setText("usuario/contraseña incorrecto");
                            passwordField.setText("");
                            }
                        }
                    });
            }

            */
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //Leemos la respuesta http con el objeto Json y lo convertimos en String. Solo
                //se puede llamar una vez porque consume el stream interno
                String res = response.body().string();
                System.out.println("JSON RECIBIDO: " + res);
                //volvemos al hilo principal para cambios en la UI
                javafx.application.Platform.runLater(() -> {
                    try {
                        /*Codigo para simular login correcto. Volver a comentar hasta donde se indica
                        para volver al login
                         */
                        /*String testJSON = "{\"email\": \"test@gmail.com\", \"idToken\": \"fake_token_123\" }";
                        JSONObject jsonObject = new JSONObject(testJSON);
                        String email = jsonObject.getString("email");
                        String idToken = jsonObject.getString("idToken");
                        goHome();*/
                        /*Comentar hasta aqui. y lo de abajo hasta el catch cuando se descomente esa parte*/
                        //Convertimos el string en un objeto Json

                        JSONObject obj = new JSONObject(res);
                        //Si login succesful->guardamos email , idtoken(autenticar usuario)
                        //cambiamos de escena y vamos a home
                        if (response.isSuccessful()) {
                            String email = obj.getString("email");
                            String id = obj.getString("localId");
                            String idToken = obj.getString("idToken");
                            System.out.println("LOGIN OK: " + res);
                            UserModel currentUser = new UserModel(id,email,null,idToken);
                            Sesion.setUsuario_actual(currentUser);
                            System.out.println(Sesion.getUserId());
                            goHome();


                        } else {//login -> failure
                            //Recuperamos el motivo de error
                            JSONObject error = obj.getJSONObject("error");
                            //lo convertimos a string
                            String mensaje_error = error.getString("message");
                            emailField.setText(mensaje_error);
                            passwordField.setText("");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });




    }
    @FXML
    public void initialize(){
        loginButton.setDefaultButton(true);
    }
    @FXML
    public void goRegister() {
        SceneManager.getInstance().switchTo("register.fxml");
    }
}
