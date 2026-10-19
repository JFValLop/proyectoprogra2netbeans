/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.api;

import com.umg.api.ui.PrincipalController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author juanfranciscovaldezlopez
 */
public class App extends Application {

    private static Stage ventana;
    private static PrincipalController principal;
    private static Runnable alSalir;

    @Override
    public void start(Stage stage) throws Exception {
        ventana = stage;
        stage.setTitle("Agencia FVP");
        cambiarPantalla("Login.fxml");
        stage.show();
    }

    public static void cambiarPantalla(String fxml) throws Exception {
        if (fxml.equals("Login.fxml")) {
            salirDePantalla();
            principal = null;
            Parent root = FXMLLoader.load(App.class.getResource("/com/umg/api/ui/Login.fxml"));
            ventana.setScene(new Scene(root));
            ventana.centerOnScreen();
            return;
        }
        if (principal == null) {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/umg/api/ui/Principal.fxml"));
            Parent root = loader.load();
            principal = loader.getController();
            ventana.setScene(new Scene(root));
            ventana.centerOnScreen();
        }
        salirDePantalla();
        Region pantalla = FXMLLoader.load(App.class.getResource("/com/umg/api/ui/" + fxml));
        pantalla.setPrefSize(0, 0);
        principal.mostrar(pantalla, fxml);
    }

    public static void mostrar(Region pantalla, String nombre) {
        salirDePantalla();
        pantalla.setPrefSize(0, 0);
        principal.mostrar(pantalla, nombre);
    }

    public static void alSalir(Runnable accion) {
        alSalir = accion;
    }

    private static void salirDePantalla() {
        if (alSalir != null) {
            Runnable accion = alSalir;
            alSalir = null;
            accion.run();
        }
    }

    public static PrincipalController getPrincipal() {
        return principal;
    }

    public static void abrirDialogo(String fxml, String titulo) throws Exception {
        Parent root = FXMLLoader.load(App.class.getResource("/com/umg/api/ui/" + fxml));
        Scene escena = new Scene(root);
        escena.setFill(Color.web("#141311"));

        Stage dialogo = new Stage();
        dialogo.setTitle(titulo);
        dialogo.initOwner(ventana);
        dialogo.initModality(Modality.APPLICATION_MODAL);
        dialogo.setScene(escena);
        dialogo.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
