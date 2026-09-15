package com.gasmanager.nomina.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class NominaDTO {

    private Long id;

    private String folioNomina;

    private LocalDate periodoInicio;

    private LocalDate periodoFin;

    private LocalDate fechaPago;

    private LocalDateTime fechaProcesamiento;

    private Integer totalEmpleados;

    private BigDecimal totalSueldos;

    private BigDecimal totalHorasExtras;

    private BigDecimal totalBonos;

    private BigDecimal totalDeducciones;

    private BigDecimal totalImpuestos;

    private BigDecimal totalNeto;

    private String estado;

    private String observaciones;

    @Builder.Default
    private List<NominaDetalleDTO> detalles = new ArrayList<>();
}