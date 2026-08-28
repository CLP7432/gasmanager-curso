package com.gasmanager.clientes.controllers;

import com.gasmanager.clientes.dto.AbonoCreditoDTO;
import com.gasmanager.clientes.dto.CreditoDTO;
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
    @GetMapping("/{id}")
    public ResponseEntity<CreditoDTO> obtener(@PathVariable Long id){
        return ResponseEntity.ok(creditoService.obtener(id));
    }
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<CreditoDTO>>listarPorCliente(@PathVariable Long clienteId){
        return ResponseEntity.ok(creditoService.listarPorCliente(clienteId));
    }
    @PostMapping("/{creditoId}/abonos")
    public ResponseEntity<CreditoDTO> registrarAbono(
            @PathVariable Long creditoId,
            @Valid @RequestBody AbonoCreditoDTO dto){

        CreditoDTO actualizado = creditoService
                .registrarAbono(creditoId, dto);
        return ResponseEntity.ok(actualizado);
    }
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> manejarConflicto(
            IllegalStateException ex){
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ex.getMessage());
    }


}
