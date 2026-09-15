package com.gasmanager.ventas.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MangueraDTO {

    private Long id;
    private String codigo;
    private String nombre;
    private String tipoCombustible;
    private Long combustibleId;
    private BigDecimal lecturaActual;
    private Boolean activo;
}