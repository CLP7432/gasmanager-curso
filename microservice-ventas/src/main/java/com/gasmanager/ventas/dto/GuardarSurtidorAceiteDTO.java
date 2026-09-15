package com.gasmanager.ventas.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuardarSurtidorAceiteDTO {

    private Long despachadorId;
    private String despachadorNombre;
    private Long dispensarioId;
    private String nombre;
    private Boolean activo;
    private List<SurtidorAceiteItemEntradaDTO> items;
}
