package com.facturacion.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

/**
 * Obtiene la fecha/hora de emisión desde el servicio externo; si falla, usa {@link Date} (consigna).
 */
@Service
public class WorldClockService {

    public static final String WORLD_CLOCK_URL = "http://worldclockapi.com/api/json/utc/now";

    private final RestTemplate restTemplate;

    public WorldClockService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public LocalDateTime obtenerFechaHoraEmision() {
        try {
            JsonNode root = restTemplate.getForObject(WORLD_CLOCK_URL, JsonNode.class);
            if (root != null && root.has("currentDateTime") && !root.get("currentDateTime").isNull()) {
                String iso = root.get("currentDateTime").asText();
                return Instant.parse(iso).atZone(ZoneOffset.UTC).toLocalDateTime();
            }
        } catch (Exception ignored) {
            // fallback
        }
        Date ahora = new Date();
        return Instant.ofEpochMilli(ahora.getTime()).atZone(ZoneOffset.UTC).toLocalDateTime();
    }
}
