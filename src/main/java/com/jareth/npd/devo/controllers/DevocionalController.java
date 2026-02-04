package com.jareth.npd.devo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.jareth.npd.devo.model.Devo;
import com.jareth.npd.devo.model.DevoDTO;
import com.jareth.npd.devo.services.DevoService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/devocionales")
// El CrossOrigin es vital para que tu Angular en GitHub Pages pueda entrar
@CrossOrigin(origins = "*")
public class DevocionalController {

    @Autowired
    private DevoService service;

    /**
     * Endpoint principal para obtener el devocional del día.
     * Ejemplo: GET /api/devocionales/2026-02-04
     */
    @GetMapping("/{date}")
    public ResponseEntity<?> getDevo(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            Devo devo = service.getDevoByDate(date);
            if (devo == null)
                return ResponseEntity.notFound().build();

            // Creamos la respuesta con ambos formatos
            DevoDTO response = new DevoDTO(
                    devo,
                    service.getDevoByPlatform(devo, "whatsapp"),
                    service.getDevoByPlatform(devo, "web"));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/range")
    public ResponseEntity<List<DevoDTO>> getByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<Devo> devos = service.getDevosByRange(fromDate, toDate);

        // Transformamos cada Devo en un DevoDTO con sus mensajes
        List<DevoDTO> response = devos.stream().map(devo -> {
            String ws = service.getDevoByPlatform(devo, "whatsapp");
            String web = service.getDevoByPlatform(devo, "web");
            return new DevoDTO(devo, ws, web);
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDevo(@PathVariable Long id) {
        try {
            service.deleteDevo(id);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Devocional eliminado con éxito");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al eliminar: " + e.getMessage()));
        }
    }

    /**
     * Endpoint de prueba para verificar que el backend está vivo en Render
     */
    @GetMapping("/health")
    public String healthCheck() {
        return "Backend de Devocionales funcionando correctamente";
    }
}