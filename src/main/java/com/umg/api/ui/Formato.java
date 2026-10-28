/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.api.ui;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 *
 * @author juanfranciscovaldezlopez
 */
public class Formato {

    private static final String[] DIAS = {"LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB", "DOM"};
    private static final String[] MESES = {"ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC"};
    private static final String[] COLORES = {"#FF5C8A", "#3CC8E0", "#A18CFF", "#5BD68A", "#6C9BFF", "#E879F9"};

    public static String dinero(double monto) {
        return "Q " + String.format("%,.2f", monto);
    }

    public static double sinIva(double total) {
        return Math.round(total / 1.12 * 100) / 100.0;
    }

    public static double iva(double total) {
        return total - sinIva(total);
    }

    public static String dineroCorto(double monto) {
        if (monto == Math.floor(monto)) {
            return "Q " + String.format("%,.0f", monto);
        }
        return dinero(monto);
    }

    public static LocalDate fecha(String fecha) {
        if (fecha == null || fecha.length() < 10) {
            return null;
        }
        return LocalDate.parse(fecha.substring(0, 10));
    }

    public static String hora(String hora) {
        if (hora == null || hora.length() < 5) {
            return "";
        }
        return hora.substring(0, 5);
    }

    public static String dia(LocalDate f) {
        return f == null ? "" : DIAS[f.getDayOfWeek().getValue() - 1];
    }

    public static String mes(LocalDate f) {
        return f == null ? "" : MESES[f.getMonthValue() - 1];
    }

    public static String fechaLarga(String fecha, String hora) {
        LocalDate f = fecha(fecha);
        if (f == null) {
            return "";
        }
        String texto = dia(f).toLowerCase() + " " + f.getDayOfMonth() + " " + mes(f).toLowerCase() + " " + f.getYear();
        return hora(hora).isEmpty() ? texto : texto + " · " + hora(hora) + " h";
    }

    public static String fechaCorta(String fecha, String hora) {
        LocalDate f = fecha(fecha);
        if (f == null) {
            return "";
        }
        String texto = dia(f).toLowerCase() + " " + f.getDayOfMonth() + " " + mes(f).toLowerCase();
        return hora(hora).isEmpty() ? texto : texto + " · " + hora(hora);
    }

    public static String momento(String instante) {
        if (instante == null) {
            return "";
        }
        LocalDateTime t = ZonedDateTime.parse(instante).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        return dia(t.toLocalDate()).toLowerCase() + " " + t.getDayOfMonth() + " " + mes(t.toLocalDate()).toLowerCase() + " " + t.getYear()
                + " · " + String.format("%02d:%02d", t.getHour(), t.getMinute());
    }

    public static String color(String color, int indice) {
        if (color != null && !color.isEmpty()) {
            return color;
        }
        return COLORES[Math.abs(indice) % COLORES.length];
    }

    public static String iniciales(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "?";
        }
        String[] partes = nombre.trim().split("\\s+");
        String r = partes[0].substring(0, 1);
        if (partes.length > 1) {
            r += partes[1].substring(0, 1);
        }
        return r.toUpperCase();
    }

    public static String rol(String rol) {
        if (rol == null) {
            return "";
        }
        if ("ADMIN".equalsIgnoreCase(rol) || "Administrador".equalsIgnoreCase(rol)) {
            return "Administrador";
        }
        return "Vendedor";
    }
}
