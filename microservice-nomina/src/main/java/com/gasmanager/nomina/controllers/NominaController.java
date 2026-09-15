package com.gasmanager.nomina.controllers;

import com.gasmanager.nomina.dto.NominaDTO;
import com.gasmanager.nomina.dto.ProcesarNominaRequestDTO;
import com.gasmanager.nomina.services.NominaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nominas")
@RequiredArgsConstructor
public class NominaController {

    private final NominaService nominaService;

    @GetMapping
    public ResponseEntity<List<NominaDTO>> listar() {
        return ResponseEntity.ok(nominaService.listar());
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<NominaDTO>> listarPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(nominaService.listarPorEstado(estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NominaDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(nominaService.obtenerPorId(id));
    }

    @PostMapping("/procesar")
    public ResponseEntity<NominaDTO> procesar(@Valid @RequestBody ProcesarNominaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(nominaService.procesar(request));
    }

    @PostMapping("/{id}/marcar-pagada")
    public ResponseEntity<NominaDTO> marcarPagada(@PathVariable Long id) {
        return ResponseEntity.ok(nominaService.marcarComoPagada(id));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<NominaDTO> cancelar(@PathVariable Long id,
                                              @RequestParam(required = false) String motivo) {
        return ResponseEntity.ok(nominaService.cancelar(id, motivo));
    }
}