/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *
 * @author juanfranciscovaldezlopez
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Estadio {
    
    private int id;
    private String nombre;
    private int accesos;
    private int capacidad;
    private String tipo;

    public Estadio() {
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @param nombre the nombre to set
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return nombre + " · " + Estadio.nombreTipo(tipo);
    }

    public static String nombreTipo(String tipo) {
        if ("ESTADIO".equals(tipo)) {
            return "Estadio";
        }
        if ("EXPLANADA".equals(tipo)) {
            return "Explanada";
        }
        return "Teatro";
    }
    
    
    

    /**
     * @return the tipo
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * @param tipo the tipo to set
     */
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getAccesos() {
        return accesos;
    }

    public void setAccesos(int accesos) {
        this.accesos = accesos;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }
}
