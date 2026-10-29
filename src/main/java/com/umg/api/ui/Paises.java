package com.umg.api.ui;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Paises {

    private static final Locale ES = Locale.of("es");
    private static final Map<String, String> codigos = new TreeMap<>(Collator.getInstance(ES));
    private static final Map<String, Image> banderas = new HashMap<>();

    static {
        for (String codigo : Locale.getISOCountries()) {
            codigos.put(Locale.of("", codigo).getDisplayCountry(ES), codigo);
        }
    }

    public static List<String> nombres() {
        return new ArrayList<>(codigos.keySet());
    }

    public static Image bandera(String pais) {
        String codigo = codigos.get(pais);
        if (codigo == null) {
            return null;
        }
        if (!banderas.containsKey(codigo)) {
            banderas.put(codigo, new Image("https://flagcdn.com/w40/" + codigo.toLowerCase() + ".png", true));
        }
        return banderas.get(codigo);
    }

    public static class Celda extends ListCell<String> {

        private final ImageView imagen = new ImageView();

        public Celda() {
            imagen.setFitWidth(22);
            imagen.setFitHeight(15);
            imagen.setPreserveRatio(true);
        }

        @Override
        protected void updateItem(String pais, boolean vacio) {
            super.updateItem(pais, vacio);
            if (vacio || pais == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(pais);
                imagen.setImage(bandera(pais));
                setGraphic(imagen);
            }
        }
    }
}
