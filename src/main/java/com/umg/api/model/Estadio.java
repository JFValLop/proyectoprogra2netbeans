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
    private String pais;
    private String ciudad;
    private String direccion;

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
        String texto = nombre + " · " + Estadio.nombreTipo(tipo);
        if (ciudad != null && !ciudad.isBlank()) {
            texto += " · " + ciudad + ", " + pais;
        }
        return texto;
    }

    public String getNombreConLugar() {
        if (ciudad == null || ciudad.isBlank()) {
            return nombre;
        }
        return nombre + " · " + ciudad + ", " + pais;
    }

    public String getUbicacion() {
        String texto = "";
        for (String parte : new String[]{direccion, ciudad, pais}) {
            if (parte != null && !parte.isBlank()) {
                texto += (texto.isEmpty() ? "" : ", ") + parte;
            }
        }
        return texto;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
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
