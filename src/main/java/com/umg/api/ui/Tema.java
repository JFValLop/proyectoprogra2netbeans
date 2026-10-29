package com.umg.api.ui;

import java.util.prefs.Preferences;
import javafx.scene.Parent;

public class Tema {

    private static final String CLARO = Tema.class.getResource("claro.css").toExternalForm();
    private static final Preferences preferencias = Preferences.userNodeForPackage(Tema.class);

    public static boolean esClaro() {
        return preferencias.getBoolean("modoClaro", false);
    }

    public static void cambiar(Parent raiz) {
        preferencias.putBoolean("modoClaro", !esClaro());
        aplicar(raiz);
    }

    public static void aplicar(Parent raiz) {
        raiz.getStylesheets().remove(CLARO);
        if (esClaro()) {
            raiz.getStylesheets().add(CLARO);
        }
    }
}
