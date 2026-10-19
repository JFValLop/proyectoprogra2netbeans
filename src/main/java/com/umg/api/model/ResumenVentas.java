/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.api.model;

/**
 *
 * @author juanfranciscovaldezlopez
 */
public class ResumenVentas {

    private int totalVentas;
    private int totalBoletos;
    private double totalMonto;

    public ResumenVentas() {
    }

    public int getTotalVentas() {
        return totalVentas;
    }

    public void setTotalVentas(int totalVentas) {
        this.totalVentas = totalVentas;
    }

    public int getTotalBoletos() {
        return totalBoletos;
    }

    public void setTotalBoletos(int totalBoletos) {
        this.totalBoletos = totalBoletos;
    }

    public double getTotalMonto() {
        return totalMonto;
    }

    public void setTotalMonto(double totalMonto) {
        this.totalMonto = totalMonto;
    }
}
