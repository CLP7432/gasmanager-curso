package com.gasmanager.nomina.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepartamentoDTO {

    private Long id;

    @NotBlank(message = "El nombre del departamento es obligatorio")
    private String nombre;

    private String descripcion;

    private Boolean activo;
}