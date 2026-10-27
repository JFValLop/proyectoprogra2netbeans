/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.api;

import com.umg.api.model.Concierto;
import com.umg.api.model.Orden;
import com.umg.api.model.Usuario;
import com.umg.api.model.Venta;

/**
 *
 * @author juanfranciscovaldezlopez
 */
public class Sesion {

    private static Usuario usuario;
    private static Concierto concierto;
    private static Orden orden;
    private static Venta venta;
    private static long ultimoMovimiento = System.currentTimeMillis();

    public static void moverse() {
        ultimoMovimiento = System.currentTimeMillis();
    }

    public static boolean estaAusente() {
        return System.currentTimeMillis() - ultimoMovimiento > 5 * 60 * 1000;
    }

    public static Usuario getUsuario() {
        return usuario;
    }

    public static void setUsuario(Usuario usuario) {
        Sesion.usuario = usuario;
    }

    public static boolean esAdmin() {
        return usuario != null && ("ADMIN".equalsIgnoreCase(usuario.getRol()) || "Administrador".equalsIgnoreCase(usuario.getRol()));
    }

    public static Concierto getConcierto() {
        return concierto;
    }

    public static void setConcierto(Concierto concierto) {
        Sesion.concierto = concierto;
    }

    public static Orden getOrden() {
        return orden;
    }

    public static void setOrden(Orden orden) {
        Sesion.orden = orden;
    }

    public static Venta getVenta() {
        return venta;
    }

    public static void setVenta(Venta venta) {
        Sesion.venta = venta;
    }

    public static void cerrar() {
        usuario = null;
        concierto = null;
        orden = null;
        venta = null;
    }
}
