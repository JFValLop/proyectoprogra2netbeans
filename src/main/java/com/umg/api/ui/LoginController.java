package com.umg.api.ui;

import com.umg.api.App;
import com.umg.api.Sesion;
import com.umg.api.model.Usuario;
import com.umg.api.service.AuthService;
import java.net.ConnectException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.media.MediaView;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private Button btnIngresar;

    @FXML
    private PasswordField txtContrasena;

    @FXML
    private TextField txtUsuario;

    private final AuthService authService = new AuthService();

    @FXML
    void ingresar(ActionEvent event) {
        String usuario = txtUsuario.getText().trim();
        String contrasena = txtContrasena.getText();

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Escribe tu usuario y contraseña");
            return;
        }

        try {
            Usuario u = authService.login(usuario, contrasena);
            Sesion.setUsuario(u);
            for (Node n : txtUsuario.getScene().getRoot().lookupAll("MediaView")) {
                MediaView video = (MediaView) n;
                if (video.getMediaPlayer() != null) {
                    video.getMediaPlayer().dispose();
                }
            }
            App.cambiarPantalla("Cartelera.fxml");
        } catch (ConnectException ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo conectar con el servidor");
        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR, ex.getMessage());
            txtContrasena.clear();
        }
    }

    @FXML
    void cambiarContrasena(ActionEvent event) {
        try {
            App.abrirDialogo("RecuperarContrasena.fxml", "Cambiar contraseña");
        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo abrir la ventana: " + ex.getMessage());
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
