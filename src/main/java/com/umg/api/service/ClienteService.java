/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.api.service;

import com.umg.api.model.Cliente;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
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
public class ClienteService {

    private static final String BASE_URL = Api.URL + "/clientes";

    public Cliente buscarPorNit(String nit) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(BASE_URL + "/nit/" + URLEncoder.encode(nit, StandardCharsets.UTF_8));
            ClassicHttpResponse response = client.execute(request);

            InputStream is = response.getEntity().getContent();
            if (response.getCode() == 404) {
                return null;
            }
            if (response.getCode() != 200) {
                throw Api.error(is, response.getCode());
            }
            return Api.mapper.readValue(is, Cliente.class);
        }
    }

    public Cliente crearCliente(String nombre, String nit, String correo) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(BASE_URL + "/create/");

            Map<String, Object> datos = new HashMap<>();
            datos.put("nombre", nombre);
            datos.put("nit", nit);
            if (correo != null && !correo.isEmpty()) {
                datos.put("correo", correo);
            }
            String json = Api.mapper.writeValueAsString(datos);
            request.setEntity(EntityBuilder.create().setText(json).setContentType(ContentType.APPLICATION_JSON).build());

            ClassicHttpResponse response = client.execute(request);
            InputStream is = response.getEntity().getContent();
            if (response.getCode() != 200) {
                throw Api.error(is, response.getCode());
            }
            return Api.mapper.readValue(is, Cliente.class);
        }
    }
}
