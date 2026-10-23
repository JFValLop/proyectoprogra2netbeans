/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.api.ui;

import com.umg.api.App;
import com.umg.api.Sesion;
import javafx.scene.control.Alert;

/**
 *
 * @author juanfranciscovaldezlopez
 */
public class Barra {

    public static void ir(String fxml) {
        try {
            App.cambiarPantalla(fxml);
        } catch (Exception ex) {
            alerta(Alert.AlertType.ERROR, "No se pudo abrir la pantalla: " + ex.getMessage());
        }
    }

    public static void cerrarSesion() {
        ir("Login.fxml");
        Sesion.cerrar();
    }

    public static void actualizarTurno() {
        if (App.getPrincipal() != null) {
            App.getPrincipal().actualizarTurno();
        }
    }
    
    public static void notificar(String texto, String tipo) {
        if (App.getPrincipal() != null) {
            App.getPrincipal().notificar(texto, tipo);
        }
    }

    public static void alerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
