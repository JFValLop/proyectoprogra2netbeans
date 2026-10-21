/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *
 * @author juanfranciscovaldezlopez
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Concierto {

    private int id;
    private String gira;
    private int estadioId;
    private int artistaId;
    private String fecha;
    private String hora;
    private String estado;
    private String artista;
    private String estadio;

    public Concierto() {
    }

    public Concierto(int id, String gira, int estadioId, String hora, String fecha, int artistaId, String estado) {
        this.id = id;
        this.gira = gira;
        this.estadioId = estadioId;
        this.hora = hora;
        this.fecha = fecha;
        this.artistaId = artistaId;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGira() {
        return gira;
    }

    public void setGira(String gira) {
        this.gira = gira;
    }

    public int getEstadioId() {
        return estadioId;
    }

    public void setEstadioId(int estadioId) {
        this.estadioId = estadioId;
    }

    public int getArtistaId() {
        return artistaId;
    }

    public void setArtistaId(int artistaId) {
        this.artistaId = artistaId;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @JsonIgnore
    public String getArtista() {
        return artista;
    }

    public void setArtista(String artista) {
        this.artista = artista;
    }

    @JsonIgnore
    public String getEstadio() {
        return estadio;
    }

    public void setEstadio(String estadio) {
        this.estadio = estadio;
    }

    @Override
    public String toString() {
        if (id == 0) {
            return "Nuevo concierto";
        }
        String dia = fecha != null && fecha.length() >= 10 ? fecha.substring(0, 10) : "";
        String titulo = artista != null ? artista : "Concierto " + id;
        return titulo + (gira != null && !gira.isEmpty() ? " · " + gira : "") + " · " + dia;
    }
}
