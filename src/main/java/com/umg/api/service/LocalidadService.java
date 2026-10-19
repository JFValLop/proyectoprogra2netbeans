/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import java.io.InputStream;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.core5.http.ContentType;
import com.umg.api.model.Localidad;

/**
 *
 * @author juanfranciscovaldezlopez
 */
public class LocalidadService {

    private static final String BASE_URL = Api.URL + "/localidades";

    public List<Localidad> getLocalidades(int estadioId) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(BASE_URL + "/estadio/" + estadioId);
            ClassicHttpResponse response = client.execute(request);

            InputStream is = response.getEntity().getContent();
            if (response.getCode() != 200) {
                throw Api.error(is, response.getCode());
            }
            return Api.mapper.readValue(is, new TypeReference<List<Localidad>>(){});
        }
    }

    public void crearLocalidad(Localidad localidad) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(BASE_URL + "/create/");

            String json = Api.mapper.writeValueAsString(localidad);
            request.setEntity(EntityBuilder.create().setText(json).setContentType(ContentType.APPLICATION_JSON).build());

            ClassicHttpResponse response = client.execute(request);
            if (response.getCode() != 200) {
                throw Api.error(response.getEntity().getContent(), response.getCode());
            }
        }
    }

    public List<Localidad> getPorPlano(String plano) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(BASE_URL + "/plano/" + plano);
            ClassicHttpResponse response = client.execute(request);

            InputStream is = response.getEntity().getContent();
            if (response.getCode() != 200) {
                throw Api.error(is, response.getCode());
            }
            return Api.mapper.readValue(is, new TypeReference<List<Localidad>>(){});
        }
    }

    public Localidad actualizarLocalidad(Localidad localidad) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPut request = new HttpPut(BASE_URL + "/update/" + localidad.getId());

            Map<String, Object> datos = new HashMap<>();
            datos.put("nombre", localidad.getNombre());
            datos.put("color", localidad.getColor());
            datos.put("filas", localidad.getFilas());
            datos.put("asientos_por_fila", localidad.getAsientos_por_fila());
            datos.put("porLado", localidad.getPorLado());
            String json = Api.mapper.writeValueAsString(datos);
            request.setEntity(EntityBuilder.create().setText(json).setContentType(ContentType.APPLICATION_JSON).build());

            ClassicHttpResponse response = client.execute(request);
            InputStream is = response.getEntity().getContent();
            if (response.getCode() != 200) {
                throw Api.error(is, response.getCode());
            }
            return Api.mapper.readValue(is, Localidad.class);
        }
    }
}
