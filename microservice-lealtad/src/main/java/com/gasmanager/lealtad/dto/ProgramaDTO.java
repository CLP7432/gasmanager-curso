package com.gasmanager.lealtad.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ProgramaDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Integer puntosPorLitro;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Boolean activo;
}
