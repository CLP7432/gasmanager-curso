package com.gasmanager.ventas.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurtidorAceiteDTO {

    private Long id;
    private Long despachadorId;
    private String despachadorNombre;
    private Long dispensarioId;
    private String nombre;
    private Boolean activo;

    private List<SurtidorAceiteItemDTO> items;
}
