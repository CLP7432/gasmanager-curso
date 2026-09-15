package com.gasmanager.ventas.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbrirTurnoDTO {

    private String nombre;
    private String fechaTurno;
    private Long supervisorId;
    private String supervisorNombre;
}