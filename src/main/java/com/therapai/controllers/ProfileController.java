package com.therapai.controllers;

import com.therapai.models.UserModel;
import com.therapai.servicies.FirebaseService;
import com.therapai.utils.SceneManager;
import com.therapai.utils.Sesion;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.function.Consumer;


public class ProfileController {
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

            if (nuevoNombre == null || nuevoNombre.isBlank()) return;

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

            if (pass1 == null || pass1.isBlank()) return;
            if (!pass1.equals(pass2)) {
                System.out.println("Las contraseñas no coinciden");
                return;
            }

            FirebaseService.getDb().collection("users")
                    .document(Sesion.getUserId())
                    .update("password", pass1);
        }, true);
    }

    @FXML
    private void changeEmail() {
        abrirPopupFormulario("Cambiar email", Sesion.getUsuario_actual().getEmail(), valores -> {
            String nuevoEmail = valores[0];

            if (nuevoEmail == null || nuevoEmail.isBlank()) return;

            // Actualizar sesión
            Sesion.getUsuario_actual().setEmail(nuevoEmail);

            // Actualizar UI
            emailLabel.setText(nuevoEmail);

            // Actualizar Firestore
            FirebaseService.getDb().collection("users")
                    .document(Sesion.getUserId())
                    .update("email", nuevoEmail);
        }, false);
    }

    @FXML
    private void manageSubscription() {}

    @FXML
    private void deleteData() {
        abrirPopupConfirmacion(
                "Borrar datos",
                "¿Estás seguro de que quieres borrar todos tus datos? Esta acción no se puede deshacer.",
                () -> {
                    FirebaseService.getDb().collection("users")
                            .document(Sesion.getUserId())
                            .delete();

                    // Cerrar sesión
                    Sesion.setUsuario_actual(null);

                    // Volver al login
                    SceneManager.getInstance().switchTo("login.fxml");
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
