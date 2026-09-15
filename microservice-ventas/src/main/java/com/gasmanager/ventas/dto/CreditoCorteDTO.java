package com.gasmanager.ventas.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditoCorteDTO {

    private Long creditoId;
    private Long clienteId;
    private String clienteNombre;
    private String tipoCombustible;
    private BigDecimal litros;
    private BigDecimal importe;
    private BigDecimal aceites;
    private LocalDate fecha;
}