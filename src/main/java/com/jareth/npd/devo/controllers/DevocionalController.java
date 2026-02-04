package com.jareth.npd.devo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.jareth.npd.devo.model.Devo;
import com.jareth.npd.devo.services.DevoService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/devocionales")
// El CrossOrigin es vital para que tu Angular en GitHub Pages pueda entrar
@CrossOrigin(origins = "*") 
public class DevocionalController {

    @Autowired
    private DevoService service;

    /**
     * Endpoint principal para obtener el devocional del día.
     * Ejemplo: GET /api/devocionales/2026-02-04?plataforma=whatsapp
     */
    @GetMapping("/{fecha}")
    public ResponseEntity<?> getDevo(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(defaultValue = "web") String plataforma) {

        try {
            // 1. Obtenemos el devocional (Trae de DB o hace Scraping si no existe)
            Devo devo = service.getDevoByDate(fecha);

            if (devo == null) {
                return ResponseEntity.notFound().build();
            }

            // 2. Generamos el mensaje formateado según la plataforma elegida
            String mensaje = service.getDevoByPlatform(devo, plataforma);

            // 3. Empaquetamos todo en un Map (esto se envía como JSON a Angular)
            Map<String, Object> response = new HashMap<>();
            response.put("datos", devo);
            response.put("mensajeFormateado", mensaje);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al procesar el devocional: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Endpoint de prueba para verificar que el backend está vivo en Render
     */
    @GetMapping("/health")
    public String healthCheck() {
        return "Backend de Devocionales funcionando correctamente en Java 21";
    }
}