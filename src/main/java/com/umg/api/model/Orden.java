/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.api.model;

import java.util.List;
import java.util.Map;

/**
 *
 * @author juanfranciscovaldezlopez
 */
public class Orden {

    private Concierto concierto;
    private List<AsientoMapa> asientos;
    private Map<Integer, ZonaMapa> zonas;
    private double total;
    private Map<Integer, Integer> cantidades;
    private Map<Integer, ZonaMapa> zonasDePie;

    public Orden() {
    }

    public Concierto getConcierto() {
        return concierto;
    }

    public void setConcierto(Concierto concierto) {
        this.concierto = concierto;
    }

    public List<AsientoMapa> getAsientos() {
        return asientos;
    }

    public void setAsientos(List<AsientoMapa> asientos) {
        this.asientos = asientos;
    }

    public Map<Integer, ZonaMapa> getZonas() {
        return zonas;
    }

    public void setZonas(Map<Integer, ZonaMapa> zonas) {
        this.zonas = zonas;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public Map<Integer, Integer> getCantidades() {
        return cantidades;
    }

    public void setCantidades(Map<Integer, Integer> cantidades) {
        this.cantidades = cantidades;
    }

    public Map<Integer, ZonaMapa> getZonasDePie() {
        return zonasDePie;
    }

    public void setZonasDePie(Map<Integer, ZonaMapa> zonasDePie) {
        this.zonasDePie = zonasDePie;
    }
}
