package com.gasmanager.ventas.controllers;

import com.gasmanager.ventas.dto.AsignarDespachadorDTO;
import com.gasmanager.ventas.dto.DispensarioDTO;
import com.gasmanager.ventas.services.DispensarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dispensarios")
@RequiredArgsConstructor
public class DispensarioController {

    private final DispensarioService dispensarioService;

    @GetMapping("/completos")
    public ResponseEntity<List<DispensarioDTO>> listarCompletos() {
        return ResponseEntity.ok(dispensarioService.listarCompletos());
    }

    @GetMapping("/completo/{id}")
    public ResponseEntity<DispensarioDTO> obtenerCompleto(@PathVariable Long id) {
        return ResponseEntity.ok(dispensarioService.obtenerCompleto(id));
    }

    @PostMapping("/completo")
    public ResponseEntity<DispensarioDTO> crearCompleto(@RequestBody DispensarioDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dispensarioService.crearCompleto(dto));
    }

    @PutMapping("/completo/{id}")
    public ResponseEntity<DispensarioDTO> actualizarCompleto(@PathVariable Long id, @RequestBody DispensarioDTO dto) {
        return ResponseEntity.ok(dispensarioService.actualizarCompleto(id, dto));
    }

    @PutMapping("/{id}/despachador")
    public ResponseEntity<DispensarioDTO> asignarDespachador(@PathVariable Long id, @RequestBody AsignarDespachadorDTO dto) {
        return ResponseEntity.ok(dispensarioService.asignarDespachador(id, dto.getDespachadorId(), dto.getDespachadorNombre()));
    }

    @PutMapping("/{id}/activo")
    public ResponseEntity<DispensarioDTO> cambiarActivo(@PathVariable Long id, @RequestParam Boolean activo) {
        return ResponseEntity.ok(dispensarioService.cambiarActivo(id, activo));
    }
}