package com.gasmanager.lealtad.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PuntosVentaDTO {
    // Respuesta al acumular: 0 puntos si no hay programa activo (no es error)
    private Long ventaId;
    private String folioVenta;
    private Integer puntos;
    private String programaNombre;
    private boolean programaActivo;
}
