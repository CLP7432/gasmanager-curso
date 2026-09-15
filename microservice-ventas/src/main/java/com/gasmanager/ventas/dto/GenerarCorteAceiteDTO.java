package com.gasmanager.ventas.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerarCorteAceiteDTO {

    private Long surtidorAceiteId;
    private Long aceiteId;
    private BigDecimal sobrante;
}
