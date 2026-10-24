package com.umg.api.reportes;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import com.umg.api.reportes.Db;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.sf.jasperreports.engine.*;
/**
 *
 * @author Misael
 */
public class ReportService {
    
    private static final Map<String, JasperReport> CACHE = new ConcurrentHashMap<>();

    private static JasperReport cargar(String nombre) throws JRException {
        JasperReport jr = CACHE.get(nombre);
        if (jr == null) {
            try (InputStream in = ReportService.class
                    .getResourceAsStream("/com/umg/api/reportes/" + nombre + ".jrxml")) {
                if (in == null) throw new JRException("No se encontró " + nombre + ".jrxml");
                jr = JasperCompileManager.compileReport(in);
                CACHE.put(nombre, jr);
            } catch (java.io.IOException e) {
                throw new JRException(e);
            }
        }
        return jr;
    }

    public static JasperPrint llenar(String nombre, Map<String, Object> params) throws Exception {
        JasperReport jr = cargar(nombre);
        try (Connection con = Db.get()) {
            return JasperFillManager.fillReport(jr, params, con);
        }
    }

    public static byte[] pdf(JasperPrint jp) throws JRException {
        return JasperExportManager.exportReportToPdf(jp);
    }
}


