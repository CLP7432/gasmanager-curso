package com.gasmanager.inventarios.controllers;

import com.gasmanager.inventarios.dto.CambioPrecioDTO;
import com.gasmanager.inventarios.dto.CombustibleDTO;
import com.gasmanager.inventarios.dto.PrecioHistoricoDTO;
import com.gasmanager.inventarios.services.CombustibleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/combustibles")
@RequiredArgsConstructor
public class CombustibleController {

    private final CombustibleService combustibleService;

    @GetMapping
    public ResponseEntity<List<CombustibleDTO>> listar(){
        return ResponseEntity.ok(combustibleService.listar());
    }
    @GetMapping("/activos")
    public ResponseEntity<List<CombustibleDTO>> listarActivos(){
        return ResponseEntity.ok(combustibleService.listarActivos());
    }
    @GetMapping("/{id}")
    public ResponseEntity<CombustibleDTO> obtenerPorId(@PathVariable Long id){
        return ResponseEntity.ok(combustibleService.obtenerPorId(id));
    }
    @PostMapping
    public ResponseEntity<CombustibleDTO> crear(@Valid @RequestBody CombustibleDTO dto,
                                                @RequestHeader(value = "X-User-Id", required = false) Long idUsuario){
        return ResponseEntity.status(HttpStatus.CREATED).body(combustibleService.crear(dto, idUsuario));
    }
    @PutMapping("/{id}")
    public ResponseEntity<CombustibleDTO> actualizar(@PathVariable Long id,
                                                     @Valid @RequestBody CombustibleDTO dto,
                                                     @RequestHeader(value = "X-User-Id", required = false)Long idUsuario){
        return ResponseEntity.ok(combustibleService.actualizar(id, dto, idUsuario));
    }
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Void> toggleActivo(@PathVariable Long id,
                                             @RequestHeader(value = "X-User-Id", required = false)Long idUsuario){
        combustibleService.toggleActivo(id, idUsuario);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{id}/cambiar-precio")
    public ResponseEntity<CombustibleDTO> cambiarPrecio(@PathVariable Long id,
                                                        @Valid @RequestBody CambioPrecioDTO dto,
                                                        @RequestHeader(value = "X-User-Id", required = false)Long idUsuario){
        return ResponseEntity.ok(combustibleService.cambiarPrecio(id, dto, idUsuario));
    }
    @GetMapping("/{id}/historial")
    public ResponseEntity<List<PrecioHistoricoDTO>> listarHistorial(@PathVariable Long id){
        return ResponseEntity.ok(combustibleService.listarHistorial(id));
    }

}
