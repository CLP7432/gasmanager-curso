package com.gasmanager.inventarios.controllers;

import com.gasmanager.inventarios.dto.CompraDTO;
import com.gasmanager.inventarios.dto.DescargaPipaDTO;
import com.gasmanager.inventarios.dto.PendienteCargaDTO;
import com.gasmanager.inventarios.services.CompraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;

    @GetMapping
    public ResponseEntity<List<CompraDTO>> listar() {
        return ResponseEntity.ok(compraService.listar());
    }

    @GetMapping("/proveedor/{proveedorId}")
    public ResponseEntity<List<CompraDTO>> listarPorProveedor(@PathVariable Long proveedorId) {
        return ResponseEntity.ok(compraService.listarPorProveedor(proveedorId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompraDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(compraService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<CompraDTO> registrar(@Valid @RequestBody CompraDTO dto,
                                               @RequestHeader(value = "X-User-Id", required = false) Long idUsuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(compraService.registrar(dto, idUsuario));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Void> toggleActivo(@PathVariable Long id) {
        compraService.toggleActivo(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/pendientes-carga")
    public ResponseEntity<List<PendienteCargaDTO>> listarPendientesCarga() {
        return ResponseEntity.ok(compraService.listarPendientesCarga());
    }

    @PostMapping("/descargar-pipa")
    public ResponseEntity<PendienteCargaDTO> descargarPipa(@Valid @RequestBody DescargaPipaDTO dto,
                                                           @RequestHeader(value = "X-User-Id", required = false) Long idUsuario) {
        return ResponseEntity.ok(compraService.descargarPipa(dto, idUsuario));
    }

    @GetMapping("/reporte")
    public ResponseEntity<List<CompraDTO>> reporte(@RequestParam int anio,@RequestParam int mes){
        return ResponseEntity.ok(compraService.listarPorMes(anio, mes));
    }
}