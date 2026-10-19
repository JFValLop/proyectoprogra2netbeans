/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.api.model;

/**
 *
 * @author juanfranciscovaldezlopez
 */
public class AsientoMapa {

    private int id;
    private String bloque;
    private String fila;
    private int numero;
    private String estado;
    private Integer retenidoPor;

    public AsientoMapa() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBloque() {
        return bloque;
    }

    public void setBloque(String bloque) {
        this.bloque = bloque;
    }

    public String getFila() {
        return fila;
    }

    public void setFila(String fila) {
        this.fila = fila;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getRetenidoPor() {
        return retenidoPor;
    }

    public void setRetenidoPor(Integer retenidoPor) {
        this.retenidoPor = retenidoPor;
    }

    public String getEtiqueta() {
        return bloque + "-" + fila + numero;
    }
}
