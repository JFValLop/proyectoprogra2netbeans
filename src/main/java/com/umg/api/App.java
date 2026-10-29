/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.api;

import com.umg.api.service.Usuario_Service;
import java.awt.Taskbar;
import javax.imageio.ImageIO;
import javafx.scene.image.Image;
import com.umg.api.ui.PrincipalController;
import com.umg.api.ui.Tema;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.InputEvent;
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
    private static HostServices servicios;
    private static PrincipalController principal;
    private static Runnable alSalir;

    @Override
    public void start(Stage stage) throws Exception {
        ventana = stage;
        servicios = getHostServices();
        stage.setTitle("Billets");
        stage.getIcons().add(new Image(App.class.getResourceAsStream("/com/umg/api/ui/logo.png")));
        ponerIconoDock();
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
            Scene escena = new Scene(root);
            escena.addEventFilter(InputEvent.ANY, e -> Sesion.moverse());
            Sesion.moverse();
            ventana.setScene(escena);
            ventana.centerOnScreen();
        }
        salirDePantalla();
        String ruta = fxml.startsWith("/") ? fxml : "/com/umg/api/ui/" + fxml;
        Region pantalla = FXMLLoader.load(App.class.getResource(ruta));
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

    public static void abrirNavegador(String direccion) {
        servicios.showDocument(direccion);
    }

    public static PrincipalController getPrincipal() {
        return principal;
    }

    public static void abrirDialogo(String fxml, String titulo) throws Exception {
        Parent root = FXMLLoader.load(App.class.getResource("/com/umg/api/ui/" + fxml));
        Tema.aplicar(root);
        Scene escena = new Scene(root);
        escena.setFill(Color.web(Tema.esClaro() ? "#FFFFFF" : "#141311"));

        Stage dialogo = new Stage();
        dialogo.setTitle(titulo);
        dialogo.getIcons().addAll(ventana.getIcons());
        dialogo.initOwner(ventana);
        dialogo.initModality(Modality.APPLICATION_MODAL);
        dialogo.setScene(escena);
        dialogo.showAndWait();
    }

    private static void ponerIconoDock() {
        try {
            if (Taskbar.isTaskbarSupported() && Taskbar.getTaskbar().isSupported(Taskbar.Feature.ICON_IMAGE)) {
                Taskbar.getTaskbar().setIconImage(ImageIO.read(App.class.getResource("/com/umg/api/ui/logo.png")));
            }
        } catch (Exception ex) {
        }
    }

    @Override
    public void stop() {
        if (Sesion.getUsuario() != null) {
            try {
                new Usuario_Service().marcarDesconectado(Sesion.getUsuario().getId());
            } catch (Exception ex) {
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
