package com.gasmanager.ventas.exceptions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> manejarNoEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarNegocio(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, String>> manejarFeign(FeignException ex) {
        String mensaje = "Error al comunicarse con el servicio de inventarios";
        if (ex.responseBody().isPresent()) {
            try {
                byte[] cuerpo = ex.responseBody().get().array();
                JsonNode nodo = new ObjectMapper().readTree(cuerpo).path("message");
                if (!nodo.isMissingNode()) {
                    mensaje = nodo.asText();
                }
            } catch (Exception ignorada) {
                // si no se puede leer el cuerpo, se queda el mensaje genérico
            }
        }
        // Diagnóstico: causa real del fallo Feign
        String detalle = ex.getClass().getSimpleName() + " status=" + ex.status() + " " + String.valueOf(ex.getMessage());
        if (detalle.length() > 300) detalle = detalle.substring(0, 300);
        return ResponseEntity.badRequest().body(Map.of("message", mensaje, "detalle", detalle));
    }
}