package com.gasmanager.ventas.controllers;

import com.gasmanager.ventas.dto.VentaDTO;
import com.gasmanager.ventas.services.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;

    @GetMapping
    public ResponseEntity<List<VentaDTO>> listar(){
        return ResponseEntity.ok(ventaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentaDTO> obtenerPorId(@PathVariable Long id){
        return ResponseEntity.ok(ventaService.obtenerPorId(id));
    }
    @GetMapping("/folio/{folio}")
    public ResponseEntity<VentaDTO> obtenerPorFolio(@PathVariable String folio){
        return ResponseEntity.ok(ventaService.obtenerPorFolio(folio));
    }
    @PostMapping
    public ResponseEntity<VentaDTO> registrar(@Valid @RequestBody VentaDTO dto,
                                              @RequestHeader(value = "X-User-Id",required = false) Long idUsuario){
        return ResponseEntity.status(HttpStatus.CREATED).body(ventaService.registrar(dto, idUsuario));
    }
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<VentaDTO> cancelar(@PathVariable Long id,
                                             @RequestHeader(value = "X-User-Id", required = false) Long idUsuario){
        return ResponseEntity.ok(ventaService.cancelar(id, idUsuario));
    }
    @GetMapping("/reporte")
    public ResponseEntity<List<VentaDTO>> reporteMensual(@RequestParam int anio, @RequestParam int mes){
        return ResponseEntity.ok(ventaService.reporteMensual(anio, mes));
    }
    @GetMapping("/turno/{turnoId}")
    public ResponseEntity<List<VentaDTO>> porTurno(@PathVariable Long turnoId) {
        return ResponseEntity.ok(ventaService.porTurno(turnoId));
    }

}
