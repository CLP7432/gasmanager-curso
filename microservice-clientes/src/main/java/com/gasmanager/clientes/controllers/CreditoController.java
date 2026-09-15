package com.gasmanager.clientes.controllers;

import com.gasmanager.clientes.dto.AbonoCreditoDTO;
import com.gasmanager.clientes.dto.CreditoDTO;
import com.gasmanager.clientes.enums.EstadoCredito;
import com.gasmanager.clientes.services.CreditoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/creditos")
@RequiredArgsConstructor
public class CreditoController {

    private final CreditoService creditoService;

    @PostMapping
    public ResponseEntity<CreditoDTO> crear(@Valid @RequestBody CreditoDTO dto){
        CreditoDTO creado = creditoService.crearCredito(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(creado);
    }

    @GetMapping
    public ResponseEntity<List<CreditoDTO>> listarTodos(){
        return ResponseEntity.ok(creditoService.listarTodos());
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<CreditoDTO>> listarPorCliente(@PathVariable Long clienteId){
        return ResponseEntity.ok(creditoService.listarPorCliente(clienteId));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<CreditoDTO>> listarPorEstado(@PathVariable EstadoCredito estado){
        return ResponseEntity.ok(creditoService.listarPorEstado(estado));
    }

    @GetMapping("/activos-con-saldo")
    public ResponseEntity<List<CreditoDTO>> listarActivosConSaldo(){
        return ResponseEntity.ok(creditoService.listarActivosConSaldo());
    }

    @GetMapping("/vencidos")
    public ResponseEntity<List<CreditoDTO>> listarVencidos(){
        return ResponseEntity.ok(creditoService.listarVencidos());
    }

    @GetMapping("/{id}/abonos")
    public ResponseEntity<List<AbonoCreditoDTO>> listarAbonos(@PathVariable Long id){
        return ResponseEntity.ok(creditoService.listarAbonos(id));
    }
    @GetMapping("/abonos")
    public ResponseEntity<List<AbonoCreditoDTO>> listarTodosAbonos(){
        return ResponseEntity.ok(creditoService.listarTodosAbonos());
    }
    @GetMapping("/{id}")
    public ResponseEntity<CreditoDTO> obtener(@PathVariable Long id){
        return ResponseEntity.ok(creditoService.obtener(id));
    }

    @PostMapping("/{creditoId}/abonos")
    public ResponseEntity<CreditoDTO> registrarAbono(
            @PathVariable Long creditoId,
            @Valid @RequestBody AbonoCreditoDTO dto){
        CreditoDTO actualizado = creditoService
                .registrarAbono(creditoId, dto);
        return ResponseEntity.ok(actualizado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CreditoDTO> actualizar(
            @PathVariable Long id, @Valid @RequestBody CreditoDTO dto){
        return ResponseEntity.ok(creditoService.actualizarCredito(id, dto));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<CreditoDTO> cancelar(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "") String motivo){
        return ResponseEntity.ok(creditoService.cancelarCredito(id, motivo));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> manejarConflicto(
            IllegalStateException ex){
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ex.getMessage());
    }
}