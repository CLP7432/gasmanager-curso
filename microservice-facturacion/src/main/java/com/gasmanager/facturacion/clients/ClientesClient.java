package com.gasmanager.facturacion.clients;

import com.gasmanager.facturacion.dto.NotaCreditoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "microservice-clientes")
public interface ClientesClient {

    @GetMapping("/api/notas-credito/cliente/{clienteId}")
    List<NotaCreditoDTO> listarNotasPorCliente(@PathVariable("clienteId") Long clienteId);
}