package com.gasmanager.ventas.controllers;

import com.gasmanager.ventas.dto.EntregarIslaDTO;
import com.gasmanager.ventas.dto.GuardarSurtidorAceiteDTO;
import com.gasmanager.ventas.dto.StockSurtidorItemDTO;
import com.gasmanager.ventas.dto.SurtidorAceiteDTO;
import com.gasmanager.ventas.services.SurtidorAceiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SurtidorAceiteController {

    private final SurtidorAceiteService surtidorAceiteService;

    @GetMapping("/api/surtidores-aceite")
    public ResponseEntity<List<SurtidorAceiteDTO>> listar() {
        return ResponseEntity.ok(surtidorAceiteService.listar());
    }

    @GetMapping("/api/surtidores-aceite/{id}")
    public ResponseEntity<SurtidorAceiteDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(surtidorAceiteService.obtener(id));
    }

    @GetMapping("/api/surtidores-aceite/{id}/stock")
    public ResponseEntity<List<StockSurtidorItemDTO>> stock(@PathVariable Long id) {
        return ResponseEntity.ok(surtidorAceiteService.stockConAlertas(id));
    }

    @PostMapping("/api/surtidores-aceite")
    public ResponseEntity<SurtidorAceiteDTO> guardar(@RequestBody GuardarSurtidorAceiteDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(surtidorAceiteService.guardar(dto));
    }

    @PutMapping("/api/surtidores-aceite/{id}")
    public ResponseEntity<SurtidorAceiteDTO> actualizar(@PathVariable Long id, @RequestBody GuardarSurtidorAceiteDTO dto) {
        return ResponseEntity.ok(surtidorAceiteService.actualizar(id, dto));
    }

    @PostMapping("/api/surtidores-aceite/entregar")
    public ResponseEntity<Void> entregar(@RequestBody EntregarIslaDTO dto) {
        surtidorAceiteService.entregarIsla(dto);
        return ResponseEntity.noContent().build();
    }
}
