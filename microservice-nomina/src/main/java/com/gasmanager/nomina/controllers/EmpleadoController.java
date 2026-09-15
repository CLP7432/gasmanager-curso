package com.gasmanager.nomina.controllers;

import com.gasmanager.nomina.dto.EmpleadoDTO;
import com.gasmanager.nomina.services.EmpleadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/empleados")
@RequiredArgsConstructor
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    @GetMapping
    public ResponseEntity<List<EmpleadoDTO>> listar() {
        return ResponseEntity.ok(empleadoService.listar());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<EmpleadoDTO>> listarActivos() {
        return ResponseEntity.ok(empleadoService.listarActivos());
    }

    @GetMapping("/despachadores")
    public ResponseEntity<List<EmpleadoDTO>> listarDespachadores() {
        return ResponseEntity.ok(empleadoService.listarDespachadores());
    }

    @GetMapping("/puesto/{puestoId}")
    public ResponseEntity<List<EmpleadoDTO>> listarPorPuesto(@PathVariable Long puestoId) {
        return ResponseEntity.ok(empleadoService.listarPorPuesto(puestoId));
    }

    @GetMapping("/departamento/{departamentoId}")
    public ResponseEntity<List<EmpleadoDTO>> listarPorDepartamento(@PathVariable Long departamentoId) {
        return ResponseEntity.ok(empleadoService.listarPorDepartamento(departamentoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpleadoDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(empleadoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<EmpleadoDTO> crear(@Valid @RequestBody EmpleadoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(empleadoService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmpleadoDTO> actualizar(@PathVariable Long id, @Valid @RequestBody EmpleadoDTO dto) {
        return ResponseEntity.ok(empleadoService.actualizar(id, dto));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<EmpleadoDTO> desactivar(@PathVariable Long id,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaBaja,
                                                  @RequestParam(required = false) String motivo) {
        return ResponseEntity.ok(empleadoService.desactivar(id, fechaBaja, motivo));
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<EmpleadoDTO> reactivar(@PathVariable Long id) {
        return ResponseEntity.ok(empleadoService.reactivar(id));
    }
}