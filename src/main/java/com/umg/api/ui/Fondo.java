/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.api.ui;

import java.util.function.Consumer;
import javafx.application.Platform;

/**
 *
 * @author juanfranciscovaldezlopez
 */
public class Fondo {

    public interface Tarea<T> {

        T ejecutar() throws Exception;
    }

    public static <T> void cargar(Tarea<T> tarea, Consumer<T> listo, Consumer<Exception> error) {
        Thread hilo = new Thread(() -> {
            try {
                T resultado = tarea.ejecutar();
                Platform.runLater(() -> listo.accept(resultado));
            } catch (Exception ex) {
                Platform.runLater(() -> error.accept(ex));
            }
        });
        hilo.setDaemon(true);
        hilo.start();
    }
}
