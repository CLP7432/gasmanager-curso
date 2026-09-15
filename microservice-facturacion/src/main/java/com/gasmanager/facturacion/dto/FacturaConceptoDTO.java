package com.gasmanager.facturacion.dto;

import com.gasmanager.facturacion.enums.OrigenConceptoFactura;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FacturaConceptoDTO {

    private Long id;
    private OrigenConceptoFactura origen;
    private Long origenId;
    private String origenFolio;
    private String descripcion;
    private String claveProdServ;
    private String claveUnidad;
    private String unidad;
    private BigDecimal cantidad;
    private BigDecimal valorUnitario;
    private BigDecimal importe;
    private BigDecimal iva;
    private BigDecimal ieps;
    private boolean esCombustible;
    private String tipoCombustible;
}