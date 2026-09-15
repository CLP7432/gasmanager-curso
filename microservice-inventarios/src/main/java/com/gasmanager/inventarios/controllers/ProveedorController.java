package com.gasmanager.inventarios.controllers;

import com.gasmanager.inventarios.dto.ProveedorDTO;
import com.gasmanager.inventarios.services.ProveedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorService proveedorService;

    @GetMapping
    public ResponseEntity<List<ProveedorDTO>> listar() {
        return ResponseEntity.ok(proveedorService.listar());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<ProveedorDTO>> listarActivos() {
        return ResponseEntity.ok(proveedorService.listarActivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProveedorDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(proveedorService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<ProveedorDTO> crear(@Valid @RequestBody ProveedorDTO dto,
                                              @RequestHeader(value = "X-User-Id", required = false) Long idUsuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(proveedorService.crear(dto, idUsuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProveedorDTO> actualizar(@PathVariable Long id,
                                                   @Valid @RequestBody ProveedorDTO dto,
                                                   @RequestHeader(value = "X-User-Id", required = false) Long idUsuario) {
        return ResponseEntity.ok(proveedorService.actualizar(id, dto, idUsuario));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Void> toggleActivo(@PathVariable Long id,
                                             @RequestHeader(value = "X-User-Id", required = false) Long idUsuario) {
        proveedorService.toggleActivo(id, idUsuario);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        proveedorService.toggleActivo(id, null);
        return ResponseEntity.noContent().build();
    }
}