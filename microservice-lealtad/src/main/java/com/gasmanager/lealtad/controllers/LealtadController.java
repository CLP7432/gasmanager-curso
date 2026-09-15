package com.gasmanager.lealtad.controllers;

import com.gasmanager.lealtad.dto.CuentaPuntosDTO;
import com.gasmanager.lealtad.dto.PuntosVentaDTO;
import com.gasmanager.lealtad.services.LealtadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lealtad")
@RequiredArgsConstructor
public class LealtadController {

    private final LealtadService service;

    // Acumula puntos del ticket. 200 con puntos=0 si no hay programa vigente.
    @PostMapping("/transacciones/venta/{ventaId}")
    public ResponseEntity<PuntosVentaDTO> acumular(@PathVariable Long ventaId) {
        return ResponseEntity.ok(service.acumular(ventaId));
    }

    @GetMapping("/cuentas")
    public ResponseEntity<List<CuentaPuntosDTO>> cuentas() {
        return ResponseEntity.ok(service.listarCuentas());
    }

    @GetMapping("/cuentas/venta/{ventaId}")
    public ResponseEntity<?> cuentaPorVenta(@PathVariable Long ventaId) {
        return ResponseEntity.ok(service.cuentaPorVenta(ventaId)
                .map(c -> (Object) c)
                .orElse(java.util.Map.of("puntos", 0)));
    }
}
