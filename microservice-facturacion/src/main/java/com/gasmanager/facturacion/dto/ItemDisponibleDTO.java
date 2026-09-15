package com.gasmanager.facturacion.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ItemDisponibleDTO {

    private Long origenId;
    private String origenFolio;
    private String descripcion;
    private BigDecimal cantidad;
    private String unidadClave;
    private String unidad;
    private BigDecimal valorUnitario;
    private Boolean esCombustible;
    private String tipoCombustible;
    private boolean yaFacturada;
}