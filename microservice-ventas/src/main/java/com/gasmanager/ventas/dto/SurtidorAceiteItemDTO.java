package com.gasmanager.ventas.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurtidorAceiteItemDTO {

    private Long id;
    private Long aceiteId;
    private String aceiteNombre;
    private String categoria;
    private BigDecimal stockActual;
    private BigDecimal stockInicioTurno;
    private BigDecimal precioVenta;
}
