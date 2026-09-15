package com.gasmanager.ventas.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TurnoDTO {

    private Long id;
    private String codigoTurno;
    private String nombre;
    private String fechaTurno;
    private String horaInicio;
    private String horaFin;
    private String estado;
    private Long supervisorId;
    private String supervisorNombre;
}