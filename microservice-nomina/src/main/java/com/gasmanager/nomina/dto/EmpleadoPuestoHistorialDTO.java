package com.gasmanager.nomina.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class EmpleadoPuestoHistorialDTO {

    private Long id;

    private Long puestoId;

    private String puestoNombre;

    private BigDecimal salarioDiario;

    private BigDecimal salarioMensual;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    private Boolean activo;

    private String motivoCambio;
}