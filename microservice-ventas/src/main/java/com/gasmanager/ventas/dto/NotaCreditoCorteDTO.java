package com.gasmanager.ventas.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotaCreditoCorteDTO {

    private String folio;
    private LocalDate fecha;
    private String cliente;
    private String tipoCombustible;
    private BigDecimal litros;
    private BigDecimal importe;
}