/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.umg.api.model.Artista;
import com.umg.api.model.Concierto;
import com.umg.api.model.Estadio;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;

/**
 *
 * @author juanfranciscovaldezlopez
 */
public class ConciertoService {

    private static final String BASE_URL = Api.URL + "/conciertos";

    public List<Concierto> getConciertos() throws Exception {
        List<Concierto> conciertos;

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            ClassicHttpResponse response = client.execute(new HttpGet(BASE_URL));
            InputStream is = response.getEntity().getContent();
            if (response.getCode() != 200) {
                throw Api.error(is, response.getCode());
            }
            conciertos = Api.mapper.readValue(is, new TypeReference<List<Concierto>>(){});
        }

        Map<Integer, String> artistas = new HashMap<>();
        for (Artista a : getArtistas()) {
            artistas.put(a.getId(), a.getNombre());
        }
        Map<Integer, String> estadios = new HashMap<>();
        for (Estadio e : new EstadioService().getEstadios()) {
            estadios.put(e.getId(), e.getNombreConLugar());
        }
        for (Concierto c : conciertos) {
            c.setArtista(artistas.get(c.getArtistaId()));
            c.setEstadio(estadios.get(c.getEstadioId()));
        }
        return conciertos;
    }

    public List<Artista> getArtistas() throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            ClassicHttpResponse response = client.execute(new HttpGet(Api.URL + "/artista"));
            InputStream is = response.getEntity().getContent();
            if (response.getCode() != 200) {
                throw Api.error(is, response.getCode());
            }
            return Api.mapper.readValue(is, new TypeReference<List<Artista>>(){});
        }
    }

    public Concierto crearConcierto(Map<String, Object> datos) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(BASE_URL + "/create/");

            String json = Api.mapper.writeValueAsString(datos);
            request.setEntity(EntityBuilder.create().setText(json).setContentType(ContentType.APPLICATION_JSON).build());

            ClassicHttpResponse response = client.execute(request);
            InputStream is = response.getEntity().getContent();
            if (response.getCode() != 200) {
                throw Api.error(is, response.getCode());
            }
            return Api.mapper.readValue(is, Concierto.class);
        }
    }

    public void actualizarConcierto(int id, Map<String, Object> datos) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPut request = new HttpPut(BASE_URL + "/update/" + id);

            String json = Api.mapper.writeValueAsString(datos);
            request.setEntity(EntityBuilder.create().setText(json).setContentType(ContentType.APPLICATION_JSON).build());

            ClassicHttpResponse response = client.execute(request);
            if (response.getCode() != 200) {
                throw Api.error(response.getEntity().getContent(), response.getCode());
            }
        }
    }
}
