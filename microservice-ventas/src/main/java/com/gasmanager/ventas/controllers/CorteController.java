package com.gasmanager.ventas.controllers;

import com.gasmanager.ventas.dto.CorteDTO;
import com.gasmanager.ventas.dto.CorteResumenDTO;
import com.gasmanager.ventas.dto.GenerarCorteDTO;
import com.gasmanager.ventas.services.CorteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cortes")
@RequiredArgsConstructor
public class CorteController {

    private final CorteService corteService;

    @GetMapping
    public ResponseEntity<List<CorteDTO>> listar(@RequestParam(required = false) String estado) {
        return ResponseEntity.ok(corteService.listar(estado));
    }

    @GetMapping("/turno/{turnoId}")
    public ResponseEntity<List<CorteDTO>> listarPorTurno(@PathVariable Long turnoId) {
        return ResponseEntity.ok(corteService.listarPorTurno(turnoId));
    }

    @GetMapping("/resumen")
    public ResponseEntity<CorteResumenDTO> resumen(@RequestParam Long turnoId,
                                                   @RequestParam Long dispensarioId) {
        return ResponseEntity.ok(corteService.resumen(turnoId, dispensarioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CorteDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(corteService.obtener(id));
    }

    @PostMapping("/generar")
    public ResponseEntity<CorteDTO> generar(@RequestBody GenerarCorteDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(corteService.generar(dto));
    }

    @PostMapping("/{id}/validar")
    public ResponseEntity<CorteDTO> validar(@PathVariable Long id,
                                            @RequestParam(required = false) String autorizadoPor) {
        return ResponseEntity.ok(corteService.validar(id, autorizadoPor));
    }

    @PostMapping("/{id}/cerrar")
    public ResponseEntity<CorteDTO> cerrar(@PathVariable Long id) {
        return ResponseEntity.ok(corteService.cerrar(id));
    }

    @PostMapping("/reprocesar-notas")
    public ResponseEntity<?> reprocesarNotas() {
        return ResponseEntity.ok(corteService.reprocesarNotasCreditoClientes());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CorteDTO> actualizar(@PathVariable Long id, @RequestBody GenerarCorteDTO dto) {
        return ResponseEntity.ok(corteService.actualizar(id, dto));
    }
}