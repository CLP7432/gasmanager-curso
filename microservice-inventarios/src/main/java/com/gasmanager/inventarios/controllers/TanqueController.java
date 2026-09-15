package com.gasmanager.inventarios.controllers;

import com.gasmanager.inventarios.dto.TanqueDTO;
import com.gasmanager.inventarios.services.TanqueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/tanques")
@RequiredArgsConstructor
public class TanqueController {

    private final TanqueService tanqueService;

    @GetMapping
    public ResponseEntity<List<TanqueDTO>> listar(){
        return ResponseEntity.ok(tanqueService.listar());
    }
    @GetMapping("/activos")
    public ResponseEntity<List<TanqueDTO>> listarActivos(){
        return ResponseEntity.ok(tanqueService.listarActivos());
    }
    @GetMapping("/{id}")
    public ResponseEntity<TanqueDTO> obtenerPorId(@PathVariable Long id){
        return ResponseEntity.ok(tanqueService.obtenerPorId(id));
    }
    @PostMapping
    public ResponseEntity<TanqueDTO> crear(@Valid @RequestBody TanqueDTO dto,
                                           @RequestHeader(value = "X-User-Id", required = false) Long idUsuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tanqueService.crear(dto, idUsuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TanqueDTO> actualizar(@PathVariable Long id,
                                                @Valid @RequestBody TanqueDTO dto,
                                                @RequestHeader(value = "X-User-Id", required = false) Long idUsuario) {
        return ResponseEntity.ok(tanqueService.actualizar(id, dto, idUsuario));
    }

    @PostMapping("/{id}/cargar")
    public ResponseEntity<TanqueDTO> cargar(@PathVariable Long id,
                                            @RequestParam BigDecimal litros,
                                            @RequestHeader(value = "X-User-Id", required = false) Long idUsuario) {
        return ResponseEntity.ok(tanqueService.cargarLitros(id, litros, idUsuario));
    }

    @PostMapping("/{id}/descargar")
    public ResponseEntity<TanqueDTO> descargar(@PathVariable Long id,
                                               @RequestParam BigDecimal litros,
                                               @RequestHeader(value = "X-User-Id", required = false) Long idUsuario) {
        return ResponseEntity.ok(tanqueService.descargarLitros(id, litros, idUsuario));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Void> toggleActivo(@PathVariable Long id,
                                             @RequestHeader(value = "X-User-Id", required = false) Long idUsuario) {
        tanqueService.toggleActivo(id, idUsuario);
        return ResponseEntity.noContent().build();
    }
}

