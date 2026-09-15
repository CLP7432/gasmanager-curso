package com.gasmanager.lealtad.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CrearProgramaDTO {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    private String descripcion;
    @NotNull(message = "Los puntos por litro son obligatorios")
    @Min(value = 1, message = "Debe otorgar al menos 1 punto por litro")
    private Integer puntosPorLitro;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
}
