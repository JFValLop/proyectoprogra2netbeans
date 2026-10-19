/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.InputStream;
import java.util.List;
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
public class RetencionService {

    public String retener(int conciertoId, int usuarioId, List<Integer> asientos) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(Api.URL + "/retenciones/create/");

            String json = Api.mapper.writeValueAsString(Map.of("conciertoId", conciertoId, "usuarioId", usuarioId, "asientos", asientos));
            request.setEntity(EntityBuilder.create().setText(json).setContentType(ContentType.APPLICATION_JSON).build());

            ClassicHttpResponse response = client.execute(request);
            InputStream is = response.getEntity().getContent();
            if (response.getCode() != 200) {
                throw Api.error(is, response.getCode());
            }
            JsonNode respuesta = Api.mapper.readTree(is);
            return respuesta.hasNonNull("expira") ? respuesta.get("expira").asText() : null;
        }
    }
}
