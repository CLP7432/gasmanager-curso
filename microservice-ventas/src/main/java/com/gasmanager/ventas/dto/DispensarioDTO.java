package com.gasmanager.ventas.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispensarioDTO {

    private Long id;
    private String numero;
    private String nombre;
    private String ubicacion;
    private Boolean activo;
    private Long despachadorId;
    private String despachadorNombre;

    @Builder.Default
    private List<CaraDTO> caras = new ArrayList<>();
}