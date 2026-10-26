package com.umg.api.ui;

import com.umg.api.service.AuthService;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class RecuperarController {

    @FXML
    private Button btnCerrar;

    @FXML
    private TextField txtUsuario;

    @FXML
    private Button btnEnviar;

    @FXML
    private Label lblMensaje;

    @FXML
    private VBox pasoCodigo;

    @FXML
    private TextField txtCodigo;

    @FXML
    private PasswordField txtNueva;

    @FXML
    private PasswordField txtConfirmar;

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnCambiar;

    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        btnCerrar.setOnAction(e -> cerrar());
        btnCancelar.setOnAction(e -> cerrar());
        btnEnviar.setOnAction(e -> enviarCodigo());
        txtUsuario.setOnAction(e -> enviarCodigo());
        btnCambiar.setOnAction(e -> cambiarContrasena());
    }

    private void mostrarMensaje(String texto, boolean error) {
        lblMensaje.setText(texto);
        lblMensaje.getStyleClass().removeAll("c-dim", "c-green", "c-red");
        if (error) {
            lblMensaje.getStyleClass().add("c-red");
        } else {
            lblMensaje.getStyleClass().add("c-green");
        }
    }

    private void enviarCodigo() {
        String usuario = txtUsuario.getText().trim();
        if (usuario.isEmpty()) {
            mostrarMensaje("Escribe tu usuario o correo.", true);
            return;
        }

        btnEnviar.setDisable(true);
        lblMensaje.setText("Enviando código...");
        Fondo.cargar(() -> authService.pedirCodigo(usuario), mensaje -> {
            mostrarMensaje(mensaje, false);
            btnEnviar.setText("Reenviar código");
            btnEnviar.setDisable(false);
            txtUsuario.setDisable(true);
            pasoCodigo.setVisible(true);
            pasoCodigo.setManaged(true);
            btnCambiar.setDisable(false);
            txtCodigo.requestFocus();
            txtUsuario.getScene().getWindow().sizeToScene();
        }, ex -> {
            mostrarMensaje(ex.getMessage(), true);
            btnEnviar.setDisable(false);
        });
    }

    private void cambiarContrasena() {
        String codigo = txtCodigo.getText().trim();
        String nueva = txtNueva.getText();
        String confirmar = txtConfirmar.getText();

        if (codigo.length() != 6) {
            mostrarMensaje("El código tiene 6 números.", true);
            return;
        }
        if (nueva.length() < 8) {
            mostrarMensaje("La contraseña debe tener al menos 8 caracteres.", true);
            return;
        }
        if (!nueva.equals(confirmar)) {
            mostrarMensaje("Las contraseñas no coinciden.", true);
            return;
        }

        btnCambiar.setDisable(true);
        String usuario = txtUsuario.getText().trim();
        Fondo.cargar(() -> authService.cambiarContrasena(usuario, codigo, nueva), mensaje -> {
            mostrarMensaje(mensaje + ". Ya puedes iniciar sesión.", false);
            pasoCodigo.setVisible(false);
            pasoCodigo.setManaged(false);
            btnEnviar.setDisable(true);
            btnCambiar.setVisible(false);
            btnCancelar.setText("Listo");
            txtUsuario.getScene().getWindow().sizeToScene();

            PauseTransition espera = new PauseTransition(Duration.seconds(2));
            espera.setOnFinished(e -> cerrar());
            espera.play();
        }, ex -> {
            mostrarMensaje(ex.getMessage(), true);
            btnCambiar.setDisable(false);
        });
    }

    private void cerrar() {
        ((Stage) btnCancelar.getScene().getWindow()).close();
    }
}
