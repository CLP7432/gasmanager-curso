package com.gasmanager.nomina.controllers;

import com.gasmanager.nomina.dto.IncidenciaDTO;
import com.gasmanager.nomina.services.IncidenciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/incidencias")
@RequiredArgsConstructor
public class IncidenciaController {

    private final IncidenciaService incidenciaService;

    @GetMapping
    public ResponseEntity<List<IncidenciaDTO>> listar() {
        return ResponseEntity.ok(incidenciaService.listar());
    }

    @GetMapping("/empleado/{empleadoId}")
    public ResponseEntity<List<IncidenciaDTO>> listarPorEmpleado(@PathVariable Long empleadoId) {
        return ResponseEntity.ok(incidenciaService.listarPorEmpleado(empleadoId));
    }

    @GetMapping("/empleado/{empleadoId}/periodo")
    public ResponseEntity<List<IncidenciaDTO>> listarPorEmpleadoYPeriodo(@PathVariable Long empleadoId,
                                                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                                                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return ResponseEntity.ok(incidenciaService.listarPorEmpleadoYPeriodo(empleadoId, inicio, fin));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidenciaDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(incidenciaService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<IncidenciaDTO> crear(@Valid @RequestBody IncidenciaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incidenciaService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncidenciaDTO> actualizar(@PathVariable Long id, @Valid @RequestBody IncidenciaDTO dto) {
        return ResponseEntity.ok(incidenciaService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        incidenciaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}