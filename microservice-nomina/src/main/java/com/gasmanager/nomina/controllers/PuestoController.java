package com.gasmanager.nomina.controllers;

import com.gasmanager.nomina.dto.PuestoDTO;
import com.gasmanager.nomina.services.PuestoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/puestos")
@RequiredArgsConstructor
public class PuestoController {

    private final PuestoService puestoService;

    @GetMapping
    public ResponseEntity<List<PuestoDTO>> listar() {
        return ResponseEntity.ok(puestoService.listar());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<PuestoDTO>> listarActivos() {
        return ResponseEntity.ok(puestoService.listarActivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PuestoDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(puestoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<PuestoDTO> crear(@Valid @RequestBody PuestoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(puestoService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PuestoDTO> actualizar(@PathVariable Long id, @Valid @RequestBody PuestoDTO dto) {
        return ResponseEntity.ok(puestoService.actualizar(id, dto));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<PuestoDTO> toggle(@PathVariable Long id) {
        return ResponseEntity.ok(puestoService.toggle(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        puestoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}