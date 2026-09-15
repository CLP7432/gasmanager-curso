package com.gasmanager.facturacion.controllers;

import com.gasmanager.facturacion.dto.ClienteFiscalDTO;
import com.gasmanager.facturacion.services.ClienteFiscalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes-fiscales")
@RequiredArgsConstructor
public class ClienteFiscalController {

    private final ClienteFiscalService service;

    @GetMapping
    public ResponseEntity<List<ClienteFiscalDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteFiscalDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<ClienteFiscalDTO> obtenerPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(service.obtenerPorClienteId(clienteId));
    }

    @PostMapping
    public ResponseEntity<ClienteFiscalDTO> guardar(@Valid @RequestBody ClienteFiscalDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteFiscalDTO> actualizar(@PathVariable Long id, @Valid @RequestBody ClienteFiscalDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}