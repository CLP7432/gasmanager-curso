package com.gasmanager.ventas.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockSurtidorItemDTO {

    private Long aceiteId;
    private String aceiteNombre;
    private String categoria;
    private BigDecimal stockActual;
    private BigDecimal precioVenta;
    private Boolean alerta;
    private String mensaje;
}
