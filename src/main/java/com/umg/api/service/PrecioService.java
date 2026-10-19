/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import java.io.InputStream;
import java.util.List;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.core5.http.ContentType;
import com.umg.api.model.Precio;
import java.util.Map;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import java.util.HashMap;

/**
 *
 * @author juanfranciscovaldezlopez
 */
public class PrecioService {

    private static final String BASE_URL = Api.URL + "/precios";

    public List<Precio> getPrecios(int conciertoId) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(BASE_URL + "/concierto/" + conciertoId);
            ClassicHttpResponse response = client.execute(request);

            InputStream is = response.getEntity().getContent();
            if (response.getCode() != 200) {
                throw Api.error(is, response.getCode());
            }
            return Api.mapper.readValue(is, new TypeReference<List<Precio>>(){});
        }
    }

    public void guardarPrecio(int conciertoId, int localidadId, double precio) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(BASE_URL + "/create/");

            String json = Api.mapper.writeValueAsString(Map.of("conciertoId", conciertoId, "localidadId", localidadId, "precio", precio));
            request.setEntity(EntityBuilder.create().setText(json).setContentType(ContentType.APPLICATION_JSON).build());

            ClassicHttpResponse response = client.execute(request);
            if (response.getCode() != 200) {
                throw Api.error(response.getEntity().getContent(), response.getCode());
            }
        }
    }

    public void guardarPrecio(int conciertoId, int localidadId, double precio, String modo, Integer cupo) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(BASE_URL + "/create/");

            Map<String, Object> datos = new HashMap<>();
            datos.put("conciertoId", conciertoId);
            datos.put("localidadId", localidadId);
            datos.put("precio", precio);
            datos.put("modo", modo);
            datos.put("cupo", cupo);
            String json = Api.mapper.writeValueAsString(datos);
            request.setEntity(EntityBuilder.create().setText(json).setContentType(ContentType.APPLICATION_JSON).build());

            ClassicHttpResponse response = client.execute(request);
            if (response.getCode() != 200) {
                throw Api.error(response.getEntity().getContent(), response.getCode());
            }
        }
    }

    public void quitarPrecio(int conciertoId, int localidadId) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpDelete request = new HttpDelete(BASE_URL + "/concierto/" + conciertoId + "/localidad/" + localidadId);
            ClassicHttpResponse response = client.execute(request);
            if (response.getCode() != 200) {
                throw Api.error(response.getEntity().getContent(), response.getCode());
            }
        }
    }
}
