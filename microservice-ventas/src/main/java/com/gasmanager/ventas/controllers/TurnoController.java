package com.gasmanager.ventas.controllers;

import com.gasmanager.ventas.dto.AbrirTurnoDTO;
import com.gasmanager.ventas.dto.TurnoDTO;
import com.gasmanager.ventas.services.TurnoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
public class TurnoController {

    private final TurnoService turnoService;

    @GetMapping
    public ResponseEntity<List<TurnoDTO>> listar(@RequestParam(required = false) String estado) {
        return ResponseEntity.ok(turnoService.listar(estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TurnoDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(turnoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<TurnoDTO> abrir(@RequestBody AbrirTurnoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(turnoService.abrir(dto.getNombre(), dto.getFechaTurno(), dto.getSupervisorId(), dto.getSupervisorNombre()));
    }

    @PostMapping("/{id}/cerrar")
    public ResponseEntity<TurnoDTO> cerrar(@PathVariable Long id) {
        return ResponseEntity.ok(turnoService.cerrar(id));
    }
}