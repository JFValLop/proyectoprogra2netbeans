/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.api.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.InputStream;

/**
 *
 * @author juanfranciscovaldezlopez
 */
public class Api {

    public static final String URL = "https://proyecto-programacion-2-2ryp.onrender.com/api"; 
    public static final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).registerModule(new JavaTimeModule());

    public static Exception error(InputStream is, int statusCode) {
        try {
            JsonNode json = mapper.readTree(is);
            if (json != null && json.has("message")) {
                return new Exception(json.get("message").asText());
            }
        } catch (Exception ex) {
        }
        return new Exception("Error del servidor: " + statusCode);
    }
}
