package com.gasmanager.nomina.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class NominaDetalleDTO {

    private Long id;

    private Long empleadoId;

    private String empleadoCodigo;

    private String empleadoNombre;

    private String puestoNombre;

    private String departamentoNombre;

    private BigDecimal diasTrabajados;

    private BigDecimal sueldoBase;

    private BigDecimal horasExtras;

    private BigDecimal horasExtrasMonto;

    private BigDecimal faltas;

    private BigDecimal faltasDescuento;

    private BigDecimal retardoDescuento;

    private BigDecimal permisoSinGoceDescuento;

    private BigDecimal bonos;

    private BigDecimal totalGravado;

    private BigDecimal isr;

    private BigDecimal cuotaSindical;

    private BigDecimal seguroSocial;

    private BigDecimal infonavit;

    private BigDecimal otrasDeducciones;
    private BigDecimal sobrantesMonto;

    private BigDecimal totalDeducciones;

    private BigDecimal netoPagar;
}