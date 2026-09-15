package com.gasmanager.nomina.controllers;

import com.gasmanager.nomina.dto.DepartamentoDTO;
import com.gasmanager.nomina.services.DepartamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departamentos")
@RequiredArgsConstructor
public class DepartamentoController {

    private final DepartamentoService departamentoService;

    @GetMapping
    public ResponseEntity<List<DepartamentoDTO>> listar() {
        return ResponseEntity.ok(departamentoService.listar());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<DepartamentoDTO>> listarActivos() {
        return ResponseEntity.ok(departamentoService.listarActivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartamentoDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(departamentoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<DepartamentoDTO> crear(@Valid @RequestBody DepartamentoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departamentoService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartamentoDTO> actualizar(@PathVariable Long id, @Valid @RequestBody DepartamentoDTO dto) {
        return ResponseEntity.ok(departamentoService.actualizar(id, dto));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<DepartamentoDTO> toggle(@PathVariable Long id) {
        return ResponseEntity.ok(departamentoService.toggle(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        departamentoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}