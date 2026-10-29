package com.umg.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Pide a la API todo lo que muestra el panel de control (GET /api/panel). */
public class PanelService {

    // Si ya tienen la URL base en otra clase (por ejemplo la que usa VentaService), usen esa.
    private static final String BASE_URL = "https://proyecto-programacion-2-2ryp.onrender.com"; 

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();
    private static final ObjectMapper JSON = new ObjectMapper();

    public static JsonNode obtener() throws Exception {
        HttpRequest peticion = HttpRequest.newBuilder(URI.create(BASE_URL + "/api/panel"))
                .timeout(Duration.ofSeconds(40))   // Render gratis puede tardar en despertar
                .GET()
                .build();

        HttpResponse<String> respuesta = HTTP.send(peticion, HttpResponse.BodyHandlers.ofString());

        if (respuesta.statusCode() != 200) {
            String mensaje = respuesta.body();
            try {
                mensaje = JSON.readTree(respuesta.body()).path("message").asText(respuesta.body());
            } catch (Exception ignorar) {
                // el cuerpo no era JSON, se deja tal cual
            }
            throw new Exception("HTTP " + respuesta.statusCode() + ": " + mensaje);
        }
        return JSON.readTree(respuesta.body());
    }
}
