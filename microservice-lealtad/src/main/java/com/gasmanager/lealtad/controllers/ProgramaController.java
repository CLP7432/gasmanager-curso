package com.gasmanager.lealtad.controllers;

import com.gasmanager.lealtad.dto.CrearProgramaDTO;
import com.gasmanager.lealtad.dto.ProgramaDTO;
import com.gasmanager.lealtad.services.ProgramaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lealtad/programas")
@RequiredArgsConstructor
public class ProgramaController {

    private final ProgramaService service;

    @GetMapping
    public ResponseEntity<List<ProgramaDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/activo")
    public ResponseEntity<?> activo() {
        return ResponseEntity.ok(service.programaVigente()
                .map(p -> (Object) p)
                .orElse(Map.of("activo", false)));
    }

    @PostMapping
    public ResponseEntity<ProgramaDTO> crear(@Valid @RequestBody CrearProgramaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProgramaDTO> actualizar(@PathVariable Long id, @RequestBody CrearProgramaDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @PutMapping("/{id}/activar")
    public ResponseEntity<ProgramaDTO> activar(@PathVariable Long id) {
        return ResponseEntity.ok(service.activar(id));
    }

    @PutMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        service.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
