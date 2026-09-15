package com.gasmanager.ventas.clients;

import com.gasmanager.ventas.dto.CrearNotaCreditoRequestDTO;
import com.gasmanager.ventas.dto.CreditoClienteDTO;
import com.gasmanager.ventas.dto.NotaCreditoClienteDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@FeignClient(name = "microservice-clientes")
public interface ClientesClient {

    @GetMapping("/api/notas-credito/por-fechas")
    List<NotaCreditoClienteDTO> listarNotasPorFechas(
            @RequestParam("desde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam("hasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    );

    @GetMapping("/api/creditos/activos-con-saldo")
    List<CreditoClienteDTO> listarCreditosActivosConSaldo();

    @PostMapping("/api/notas-credito")
    NotaCreditoClienteDTO crearNotaCredito(@RequestBody CrearNotaCreditoRequestDTO request);
}
