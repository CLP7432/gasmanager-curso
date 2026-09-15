package com.gasmanager.ventas.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntregarIslaDTO {

    private Long surtidorAceiteId;
    private Long aceiteId;
    private BigDecimal cantidad;
    private Long turnoId;
    private Long usuarioId;
}
