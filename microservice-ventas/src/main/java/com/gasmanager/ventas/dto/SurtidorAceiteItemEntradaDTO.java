package com.gasmanager.ventas.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurtidorAceiteItemEntradaDTO {

    private Long aceiteId;
    private BigDecimal stockActual;
}
