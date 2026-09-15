package com.gasmanager.ventas.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreditoClienteDTO {
    private Long id;
    private String folioCredito;
    private Long clienteId;
    private String clienteNombre;
    private BigDecimal saldoPendiente;
    private String estado;
}