package com.gasmanager.ventas.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LineaResumenDTO {

    private String producto;
    private BigDecimal cantidad;
    private BigDecimal importe;
}