package com.gasmanager.inventarios.controllers;

import com.gasmanager.inventarios.dto.AceiteDTO;
import com.gasmanager.inventarios.dto.PrecioAceiteDTO;
import com.gasmanager.inventarios.services.AceiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/aceites")
@RequiredArgsConstructor
public class AceiteController {

    private final AceiteService aceiteService;

    @GetMapping
    public ResponseEntity<List<AceiteDTO>> listar() {
        return ResponseEntity.ok(aceiteService.listar());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<AceiteDTO>> listarActivos() {
        return ResponseEntity.ok(aceiteService.listarActivos());
    }

    @GetMapping("/stock-bajo")
    public ResponseEntity<List<AceiteDTO>> listarStockBajo() {
        return ResponseEntity.ok(aceiteService.listarStockBajo());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AceiteDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(aceiteService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<AceiteDTO> crear(@Valid @RequestBody AceiteDTO dto,
                                           @RequestHeader(value = "X-User-Id", required = false) Long idUsuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(aceiteService.crear(dto, idUsuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AceiteDTO> actualizar(@PathVariable Long id,
                                                @Valid @RequestBody AceiteDTO dto,
                                                @RequestHeader(value = "X-User-Id", required = false) Long idUsuario) {
        return ResponseEntity.ok(aceiteService.actualizar(id, dto, idUsuario));
    }

    @PutMapping("/{id}/precio")
    public ResponseEntity<AceiteDTO> actualizarPrecio(@PathVariable Long id,
                                                      @Valid @RequestBody PrecioAceiteDTO dto,
                                                      @RequestHeader(value = "X-User-Id", required = false) Long idUsuario) {
        aceiteService.actualizarPrecio(id, dto.getPrecioVenta(), idUsuario);
        return ResponseEntity.ok(aceiteService.obtenerPorId(id));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Void> toggleActivo(@PathVariable Long id,
                                             @RequestHeader(value = "X-User-Id", required = false) Long idUsuario) {
        aceiteService.toggleActivo(id, idUsuario);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{id}/aumentar-stock")
    public ResponseEntity<Void> aumentarStock(@PathVariable Long id,
                                              @RequestParam Integer cantidad,
                                              @RequestHeader(value = "X-User-Id", required = false) Long idUsuario){
        aceiteService.aumentarStock(id, cantidad, idUsuario);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{id}/disminuir-stock")
    public ResponseEntity<Void> disminuirStock(@PathVariable Long id,
                                               @RequestParam Integer cantidad,
                                               @RequestHeader(value = "X-User-Id",required = false) Long idUsuario){
        aceiteService.disminuirStock(id, cantidad, idUsuario);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id){
        aceiteService.toggleActivo(id, null);
        return ResponseEntity.noContent().build();
    }

}
