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
public class ZonaMapa {

    private int id;
    private String nombre;
    private String color;
    private String codigo;
    private String tipo;
    private int filas;
    private int asientos_por_fila;
    private Integer porLado;
    private String modo;
    private int capacidad;
    private Double precio;
    private int disponibles;
    private List<AsientoMapa> asientos;

    public ZonaMapa() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public int getDisponibles() {
        return disponibles;
    }

    public void setDisponibles(int disponibles) {
        this.disponibles = disponibles;
    }

    public boolean esDePie() {
        return "DE_PIE".equals(modo);
    }

    public List<AsientoMapa> getAsientos() {
        return asientos;
    }

    public void setAsientos(List<AsientoMapa> asientos) {
        this.asientos = asientos;
    }

    public String getModo() {
        return modo;
    }

    public void setModo(String modo) {
        this.modo = modo;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getFilas() {
        return filas;
    }

    public void setFilas(int filas) {
        this.filas = filas;
    }

    public int getAsientos_por_fila() {
        return asientos_por_fila;
    }

    public void setAsientos_por_fila(int asientos_por_fila) {
        this.asientos_por_fila = asientos_por_fila;
    }

    public Integer getPorLado() {
        return porLado;
    }

    public void setPorLado(Integer porLado) {
        this.porLado = porLado;
    }
}
