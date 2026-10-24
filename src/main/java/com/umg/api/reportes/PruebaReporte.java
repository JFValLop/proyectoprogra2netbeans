/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.api.reportes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Misael
 */
public class PruebaReporte {
    public static void main(String[] args) throws Exception {
        Map<String, Object> p = new HashMap<>();
        p.put("P_DESDE", java.sql.Date.valueOf("2026-10-01"));
        p.put("P_HASTA", java.sql.Date.valueOf("2026-10-20"));
        p.put("P_CONCIERTO", null);
        p.put("P_ARTISTA", null);
        p.put("P_VENDEDOR", null);
        p.put("P_CODIGO", "RPT-TEST");
        p.put("P_GENERADO_POR", "Andrea Morales");
        p.put("P_FILTROS", "ninguno");

        var jp = ReportService.llenar("reporte_vendedor", p);
        Files.write(Path.of("prueba.pdf"), ReportService.pdf(jp));
        System.out.println("Listo: prueba.pdf");
    }
}
