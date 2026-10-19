/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.api.model;

/**
 *
 * @author juanfranciscovaldezlopez
 */
public class Localidad {
    
    private int id;
    private String nombre;
    private int filas;
    private int asientos_por_fila;
    private Double precio;
    private int bloques;
    private String color;
    private String codigo;
    private String plano;
    private String tipo;
    private Integer porLado;
    private int conciertos;
    private int estadioId;
    private int totalAsientos;

    public Localidad() {
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

    /**
     * @return the filas
     */
    public int getFilas() {
        return filas;
    }

    /**
     * @param filas the filas to set
     */
    public void setFilas(int filas) {
        this.filas = filas;
    }

    /**
     * @return the asientos_por_fila
     */
    public int getAsientos_por_fila() {
        return asientos_por_fila;
    }

    /**
     * @param asientos_por_fila the asientos_por_fila to set
     */
    public void setAsientos_por_fila(int asientos_por_fila) {
        this.asientos_por_fila = asientos_por_fila;
    }

    /**
     * @return the precio
     */
    public Double getPrecio() {
        return precio;
    }

    /**
     * @param precio the precio to set
     */
    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    /**
     * @return the bloques
     */
    public int getBloques() {
        return bloques;
    }

    /**
     * @param bloques the bloques to set
     */
    public void setBloques(int bloques) {
        this.bloques = bloques;
    }

    /**
     * @return the color
     */
    public String getColor() {
        return color;
    }

    /**
     * @param color the color to set
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * @return the estadioId
     */
    public int getEstadioId() {
        return estadioId;
    }

    /**
     * @param estadioId the estadioId to set
     */
    public void setEstadioId(int estadioId) {
        this.estadioId = estadioId;
    }

    /**
     * @return the totalAsientos
     */
    public int getTotalAsientos() {
        return totalAsientos;
    }

    /**
     * @param totalAsientos the totalAsientos to set
     */
    public void setTotalAsientos(int totalAsientos) {
        this.totalAsientos = totalAsientos;
    }

    @Override
    public String toString() {
        return nombre;
    }

    
    

    /**
     * @return the codigo
     */
    public String getCodigo() {
        return codigo;
    }

    /**
     * @param codigo the codigo to set
     */
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    /**
     * @return the plano
     */
    public String getPlano() {
        return plano;
    }

    /**
     * @param plano the plano to set
     */
    public void setPlano(String plano) {
        this.plano = plano;
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

    /**
     * @return the porLado
     */
    public Integer getPorLado() {
        return porLado;
    }

    /**
     * @param porLado the porLado to set
     */
    public void setPorLado(Integer porLado) {
        this.porLado = porLado;
    }

    /**
     * @return the conciertos
     */
    public int getConciertos() {
        return conciertos;
    }

    /**
     * @param conciertos the conciertos to set
     */
    public void setConciertos(int conciertos) {
        this.conciertos = conciertos;
    }
}
