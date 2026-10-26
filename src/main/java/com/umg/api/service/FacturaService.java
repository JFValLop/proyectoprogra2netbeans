package com.umg.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.umg.api.reportes.ReportService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRXmlExporter;
import net.sf.jasperreports.engine.xml.JRPrintXmlLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleXmlExporterOutput;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;

/**
 * Factura: Jasper la arma una sola vez (al vender) y la exporta a XML; la API la guarda
 * en Cloudflare. Para verla, Java baja ese XML (Java -> Render -> Cloudflare) y lo carga
 * como JasperPrint. El PDF se genera al vuelo desde ese mismo XML y no se guarda en ningún lado.
 */
public class FacturaService {

    private static final String BASE_URL = Api.URL + "/facturas";

    private boolean correoEnviado = false;

    // ---------- Generar y guardar (al vender) ----------

    /** Convierte el reporte ya lleno en un XML (bytes). */
    public static byte[] xml(JasperPrint jp) throws JRException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JRXmlExporter exporter = new JRXmlExporter();
        exporter.setExporterInput(new SimpleExporterInput(jp));
        exporter.setExporterOutput(new SimpleXmlExporterOutput(out));
        exporter.exportReport();
        return out.toByteArray();
    }

    /** Busca el id de la factura que la API creó para esa venta. */
    public int buscarFacturaId(int ventaId) throws Exception {
        JsonNode lista = getJson(BASE_URL + "?ventaId=" + ventaId);
        if (!lista.isArray() || lista.size() == 0) {
            throw new Exception("La venta " + ventaId + " no tiene factura");
        }
        return lista.get(0).get("id").asInt();
    }

    /** Arma la factura con Jasper, la exporta a XML y la sube a la nube a través de la API. */
    public void generarYGuardar(int ventaId) throws Exception {
        int facturaId = buscarFacturaId(ventaId);

        Map<String, Object> params = new HashMap<>();
        params.put("VENTA_ID", ventaId);
        JasperPrint jp = ReportService.llenar("Factura", params);
        if (jp.getPages() == null || jp.getPages().isEmpty()) {
            throw new Exception("La factura de la venta " + ventaId + " salió vacía");
        }

        try {
            new VentaService().enviarCorreo(ventaId, null, pdf(jp));
            correoEnviado = true;
        } catch (Exception e) {
            System.err.println("No se pudo enviar el correo con la factura: " + e.getMessage());
        }

        byte[] xml = xml(jp);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPut request = new HttpPut(BASE_URL + "/" + facturaId + "/xml");
            request.setEntity(EntityBuilder.create()
                    .setBinary(xml)
                    .setContentType(ContentType.create("application/xml"))
                    .build());
            ClassicHttpResponse response = client.execute(request);
            InputStream is = response.getEntity().getContent();
            if (response.getCode() != 201) {
                throw Api.error(is, response.getCode());
            }
        }
    }

    /** Lo llamas justo después de crear la venta; corre aparte para no frenar la pantalla. */
    public static void guardarEnSegundoPlano(int ventaId) {
        Thread hilo = new Thread(() -> {
            FacturaService servicio = new FacturaService();
            try {
                servicio.generarYGuardar(ventaId);
                System.out.println("Factura de la venta " + ventaId + " guardada en la nube (XML)");
            } catch (Exception e) {
                System.err.println("No se pudo guardar la factura de la venta " + ventaId + ": " + e.getMessage());
                e.printStackTrace();
            }

            if (!servicio.correoEnviado) {
                try {
                    new VentaService().enviarCorreo(ventaId, null, null);
                } catch (Exception e) {
                    System.err.println("No se pudo enviar el correo de la venta " + ventaId + ": " + e.getMessage());
                }
            }
        });
        hilo.setDaemon(true);
        hilo.start();
    }

    // ---------- Gestión de ventas (consultas a la API) ----------

    /** rango: "hoy", "7" o "todo". usuarioId null = todos los vendedores. */
    public JsonNode listarVentas(String rango, Integer usuarioId) throws Exception {
        String url = BASE_URL + "/ventas?rango=" + rango + (usuarioId != null ? "&usuarioId=" + usuarioId : "");
        return getJson(url);
    }

    /** Boletos (ticket) y datos de factura de una venta. */
    public JsonNode detalleVenta(int ventaId) throws Exception {
        return getJson(BASE_URL + "/ventas/" + ventaId);
    }

    /** Baja de Cloudflare (vía la API) el XML de la factura. Devuelve null si todavía no existe. */
    public byte[] bajarXml(int facturaId) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            ClassicHttpResponse response = client.execute(new HttpGet(BASE_URL + "/" + facturaId + "/xml"));
            if (response.getCode() == 404) {
                return null;
            }
            InputStream is = response.getEntity().getContent();
            if (response.getCode() != 200) {
                throw Api.error(is, response.getCode());
            }
            return is.readAllBytes();
        }
    }

    // ---------- Mostrar / imprimir (sin volver a llenar el reporte) ----------

    /** Carga el XML descargado como un JasperPrint listo para dibujar. */
    public static JasperPrint cargarXml(byte[] xml) throws JRException {
        return JRPrintXmlLoader.load(new ByteArrayInputStream(xml));
    }

    /** PDF en memoria a partir del mismo JasperPrint. No se sube a ningún lado. */
    public static byte[] pdf(JasperPrint jp) throws JRException {
        return JasperExportManager.exportReportToPdf(jp);
    }

    public static byte[] pdfDeVenta(int ventaId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("VENTA_ID", ventaId);
            return pdf(ReportService.llenar("Factura", params));
        } catch (Exception e) {
            System.err.println("No se pudo armar la factura de la venta " + ventaId + ": " + e.getMessage());
            return null;
        }
    }

    private JsonNode getJson(String url) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            ClassicHttpResponse response = client.execute(new HttpGet(url));
            InputStream is = response.getEntity().getContent();
            if (response.getCode() != 200) {
                throw Api.error(is, response.getCode());
            }
            return Api.mapper.readTree(is);
        }
    }
}
