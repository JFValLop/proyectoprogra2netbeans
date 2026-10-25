/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.api.service;

import com.umg.api.model.ResumenVentas;
import com.umg.api.model.Venta;
import java.io.InputStream;
import java.util.Map;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;

/**
 *
 * @author juanfranciscovaldezlopez
 */
public class VentaService {

    private static final String BASE_URL = Api.URL + "/ventas";

    public Venta crearVenta(Map<String, Object> datos) throws Exception {
    try (CloseableHttpClient client = HttpClients.createDefault()) {
        HttpPost request = new HttpPost(BASE_URL + "/create/");

        String json = Api.mapper.writeValueAsString(datos);
        request.setEntity(EntityBuilder.create().setText(json).setContentType(ContentType.APPLICATION_JSON).build());

        ClassicHttpResponse response = client.execute(request);
        InputStream is = response.getEntity().getContent();
        if (response.getCode() != 200) {
            throw Api.error(is, response.getCode());
        }

        Venta venta = Api.mapper.readValue(is, Venta.class);

        // La venta ya quedó guardada: la factura corre aparte y nunca debe romperla
        try {
            FacturaService.guardarEnSegundoPlano(venta.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return venta;
    }
}

    public ResumenVentas getResumenHoy(Integer usuarioId) throws Exception {
        String url = BASE_URL + "?hoy=1" + (usuarioId != null ? "&usuarioId=" + usuarioId : "");
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            ClassicHttpResponse response = client.execute(new HttpGet(url));
            InputStream is = response.getEntity().getContent();
            if (response.getCode() != 200) {
                throw Api.error(is, response.getCode());
            }
            return Api.mapper.readValue(is, ResumenVentas.class);
        }
    }
}
