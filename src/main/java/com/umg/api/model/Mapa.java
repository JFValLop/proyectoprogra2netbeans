/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.api.model;

import java.util.List;

/**
 *
 * @author juanfranciscovaldezlopez
 */
public class Mapa {

    private int conciertoId;
    private String tipo;
    private List<ZonaMapa> localidades;

    public Mapa() {
    }

    public int getConciertoId() {
        return conciertoId;
    }

    public void setConciertoId(int conciertoId) {
        this.conciertoId = conciertoId;
    }

    public List<ZonaMapa> getLocalidades() {
        return localidades;
    }

    public void setLocalidades(List<ZonaMapa> localidades) {
        this.localidades = localidades;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
