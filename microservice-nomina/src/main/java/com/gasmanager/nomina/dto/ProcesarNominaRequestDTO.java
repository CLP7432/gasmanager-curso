package com.gasmanager.nomina.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ProcesarNominaRequestDTO {

    @NotNull(message = "El periodo de inicio es obligatorio")
    private LocalDate periodoInicio;

    @NotNull(message = "El periodo de fin es obligatorio")
    private LocalDate periodoFin;

    private LocalDate fechaPago;

    private String observaciones;
}