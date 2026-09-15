package com.gasmanager.clientes.controllers;

import com.gasmanager.clientes.dto.LiquidarNotasRequestDTO;
import com.gasmanager.clientes.dto.NotaCreditoDTO;
import com.gasmanager.clientes.services.NotaCreditoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/notas-credito")
@RequiredArgsConstructor
public class NotaCreditoController {

    private final NotaCreditoService service;

    @GetMapping
    public ResponseEntity<List<NotaCreditoDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotaCreditoDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @GetMapping("/credito/{creditoId}")
    public ResponseEntity<List<NotaCreditoDTO>> listarPorCredito(@PathVariable Long creditoId) {
        return ResponseEntity.ok(service.listarPorCredito(creditoId));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<NotaCreditoDTO>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(service.listarPorCliente(clienteId));
    }

    @GetMapping("/por-fechas")
    public ResponseEntity<List<NotaCreditoDTO>> listarPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(service.listarPorRangoFechas(desde, hasta));
    }

    @PostMapping
    public ResponseEntity<NotaCreditoDTO> crear(@Valid @RequestBody NotaCreditoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @PostMapping("/liquidar")
    public ResponseEntity<List<NotaCreditoDTO>> liquidar(@Valid @RequestBody LiquidarNotasRequestDTO dto) {
        return ResponseEntity.ok(service.liquidarNotas(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotaCreditoDTO> actualizar(@PathVariable Long id, @Valid @RequestBody NotaCreditoDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @PatchMapping("/{id}/bloquear")
    public ResponseEntity<NotaCreditoDTO> bloquear(@PathVariable Long id) {
        return ResponseEntity.ok(service.bloquear(id));
    }

    @PatchMapping("/{id}/desbloquear")
    public ResponseEntity<NotaCreditoDTO> desbloquear(@PathVariable Long id) {
        return ResponseEntity.ok(service.desbloquear(id));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> manejarConflicto(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> manejarError(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}