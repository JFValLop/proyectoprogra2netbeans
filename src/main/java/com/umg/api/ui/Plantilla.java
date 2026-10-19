/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.api.ui;

import java.util.Map;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

/**
 *
 * @author juanfranciscovaldezlopez
 */
public class Plantilla {

    private final Parent raiz;
    private final Map<String, Object> elementos;

    private Plantilla(Parent raiz, Map<String, Object> elementos) {
        this.raiz = raiz;
        this.elementos = elementos;
    }

    public static Plantilla cargar(String archivo) {
        try {
            FXMLLoader loader = new FXMLLoader(Plantilla.class.getResource("/com/umg/api/ui/plantillas/" + archivo));
            Parent raiz = loader.load();
            return new Plantilla(raiz, loader.getNamespace());
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo cargar la plantilla " + archivo, ex);
        }
    }

    public Parent getRaiz() {
        return raiz;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String id) {
        return (T) elementos.get(id);
    }
}
