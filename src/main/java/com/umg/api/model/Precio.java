/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.api.model;

/**
 *
 * @author juanfranciscovaldezlopez
 */
public class Precio {

    private int id;
    private int conciertoId;
    private int localidadId;
    private String localidad;
    private double precio;
    private String modo;
    private Integer cupo;

    public Precio() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getConciertoId() {
        return conciertoId;
    }

    public void setConciertoId(int conciertoId) {
        this.conciertoId = conciertoId;
    }

    public int getLocalidadId() {
        return localidadId;
    }

    public void setLocalidadId(int localidadId) {
        this.localidadId = localidadId;
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    @Override
    public String toString() {
        return localidad + ": Q" + String.format("%,.2f", precio);
    }

    public String getModo() {
        return modo;
    }

    public void setModo(String modo) {
        this.modo = modo;
    }

    public Integer getCupo() {
        return cupo;
    }

    public void setCupo(Integer cupo) {
        this.cupo = cupo;
    }
}
