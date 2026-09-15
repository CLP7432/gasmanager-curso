package com.gasmanager.nomina.dto;

import com.gasmanager.nomina.enums.TipoIncidencia;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class IncidenciaDTO {

    private Long id;

    @NotNull(message = "El empleado es obligatorio")
    private Long empleadoId;

    private String empleadoNombre;

    @NotNull(message = "El tipo de incidencia es obligatorio")
    private TipoIncidencia tipo;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    private BigDecimal cantidad;

    private BigDecimal monto;

    private String observaciones;

    private String autorizadoPor;
}