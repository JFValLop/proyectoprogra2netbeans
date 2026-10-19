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
import com.umg.api.model.Estadio;
import java.util.Map;

/**
 *
 * @author juanfranciscovaldezlopez
 */
public class EstadioService {

    private static final String BASE_URL = Api.URL + "/estadios";

    public List<Estadio> getEstadios() throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(BASE_URL);
            ClassicHttpResponse response = client.execute(request);

            InputStream is = response.getEntity().getContent();
            if (response.getCode() != 200) {
                throw Api.error(is, response.getCode());
            }
            return Api.mapper.readValue(is, new TypeReference<List<Estadio>>(){});
        }
    }

    public Estadio crearEstadio(String nombre, String tipo) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(BASE_URL + "/create/");

            String json = Api.mapper.writeValueAsString(Map.of("nombre", nombre, "tipo", tipo));
            request.setEntity(EntityBuilder.create().setText(json).setContentType(ContentType.APPLICATION_JSON).build());

            ClassicHttpResponse response = client.execute(request);
            InputStream is = response.getEntity().getContent();
            if (response.getCode() != 200) {
                throw Api.error(is, response.getCode());
            }
            return Api.mapper.readValue(is, Estadio.class);
        }
    }
}
