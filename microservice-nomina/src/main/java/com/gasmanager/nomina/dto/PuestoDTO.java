package com.gasmanager.nomina.dto;

import com.gasmanager.nomina.enums.RiesgoPuesto;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PuestoDTO {

    private Long id;

    @NotBlank(message = "El nombre del puesto es obligatorio")
    private String nombre;

    private String descripcion;

    private BigDecimal salarioBase;

    private BigDecimal salarioDiario;

    private RiesgoPuesto riesgoPuesto;

    private Boolean activo;
}