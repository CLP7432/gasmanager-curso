package com.gasmanager.lealtad.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CuentaPuntosDTO {
    private Long id;
    private Long ventaId;
    private String folioVenta;
    private BigDecimal litros;
    private Integer puntos;
}
