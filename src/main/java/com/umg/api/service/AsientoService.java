/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.api.service;

import com.umg.api.model.Mapa;
import java.io.InputStream;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;

/**
 *
 * @author juanfranciscovaldezlopez
 */
public class AsientoService {

    public Mapa getMapa(int conciertoId) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(Api.URL + "/asientos/concierto/" + conciertoId);
            ClassicHttpResponse response = client.execute(request);

            InputStream is = response.getEntity().getContent();
            if (response.getCode() != 200) {
                throw Api.error(is, response.getCode());
            }
            return Api.mapper.readValue(is, Mapa.class);
        }
    }
}
