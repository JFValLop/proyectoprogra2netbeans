/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
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
public class BoletoService {

    private static final String BASE_URL = Api.URL + "/boletos";

    public JsonNode validar(String codigo) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(BASE_URL + "/validar/");

            String json = Api.mapper.writeValueAsString(Map.of("codigo", codigo));
            request.setEntity(EntityBuilder.create().setText(json).setContentType(ContentType.APPLICATION_JSON).build());

            ClassicHttpResponse response = client.execute(request);
            InputStream is = response.getEntity().getContent();
            int estado = response.getCode();
            if (estado == 200 || estado == 404 || estado == 409) {
                return Api.mapper.readTree(is);
            }
            throw Api.error(is, estado);
        }
    }

    public String cancelar(int boletoId, int usuarioId, String motivo) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(BASE_URL + "/" + boletoId + "/cancelar");

            Map<String, Object> datos = new HashMap<>();
            datos.put("usuarioId", usuarioId);
            if (motivo != null && !motivo.isBlank()) {
                datos.put("motivo", motivo.trim());
            }
            String json = Api.mapper.writeValueAsString(datos);
            request.setEntity(EntityBuilder.create().setText(json).setContentType(ContentType.APPLICATION_JSON).build());

            ClassicHttpResponse response = client.execute(request);
            InputStream is = response.getEntity().getContent();
            if (response.getCode() != 200) {
                throw Api.error(is, response.getCode());
            }
            return Api.mapper.readTree(is).path("message").asText("Boleto cancelado");
        }
    }
}
