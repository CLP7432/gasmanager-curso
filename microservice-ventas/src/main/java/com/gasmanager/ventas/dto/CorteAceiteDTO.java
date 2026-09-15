package com.gasmanager.ventas.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorteAceiteDTO {

    private Long id;
    private Long surtidorAceiteId;
    private Long aceiteId;
    private String aceiteNombre;
    private String categoria;
    private BigDecimal recibidoTotal;
    private BigDecimal sobrante;
    private BigDecimal vendidos;
    private BigDecimal precioVenta;
    private BigDecimal importe;
}
