package com.gasmanager.ventas.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignarDespachadorDTO {

    private Long despachadorId;
    private String despachadorNombre;
}