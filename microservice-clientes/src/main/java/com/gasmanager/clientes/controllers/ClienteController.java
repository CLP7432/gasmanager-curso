package com.gasmanager.clientes.controllers;

import com.gasmanager.clientes.dto.ClienteDTO;
import com.gasmanager.clientes.exceptions.RecursoNoEncontradoException;
import com.gasmanager.clientes.services.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    public ResponseEntity<ClienteDTO> crear(@Valid @RequestBody ClienteDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crearCliente(dto));
    }

    @GetMapping
    public ResponseEntity<List<ClienteDTO>> listar() {
        return ResponseEntity.ok(clienteService.listarClientes());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<ClienteDTO>> listarActivos(){
        return ResponseEntity.ok(clienteService.listarActivos());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ClienteDTO>> buscarPorRazonSocial(@RequestParam String razonSocial){
        return ResponseEntity.ok(clienteService.buscarPorRazonSocial(razonSocial));
    }

    @GetMapping("/rfc/{rfc}")
    public ResponseEntity<ClienteDTO> obtenerPorRFC(@PathVariable String rfc){
        return ResponseEntity.ok(clienteService.obtenerPorRFC(rfc));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ClienteDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.obtenerCliente(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteDTO> actualizar(@PathVariable Long id, @Valid @RequestBody ClienteDTO dto) {
        return ResponseEntity.ok(clienteService.actualizarCliente(id, dto));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ClienteDTO> toggleActivo(@PathVariable Long id){
        return ResponseEntity.ok(clienteService.toggleActivo(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        clienteService.eliminarCliente(id);
        return ResponseEntity.noContent().build();
    }
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<String> manejarNoEncontrado(RecursoNoEncontradoException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> manejarDuplicado(IllegalStateException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }


}
