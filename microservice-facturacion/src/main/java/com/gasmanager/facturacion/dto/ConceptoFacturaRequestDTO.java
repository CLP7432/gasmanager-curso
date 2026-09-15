package com.gasmanager.facturacion.dto;

import com.gasmanager.facturacion.enums.OrigenConceptoFactura;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ConceptoFacturaRequestDTO {

    @NotNull(message = "El origen del concepto es obligatorio")
    private OrigenConceptoFactura origen;

    private Long origenId;

    private String origenFolio;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    @NotBlank(message = "La clave ProdServ es obligatoria")
    private String claveProdServ;

    @NotBlank(message = "La clave Unidad es obligatoria")
    private String claveUnidad;

    private String unidad;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor a cero")
    private BigDecimal cantidad;

    @NotNull(message = "El valor unitario es obligatorio")
    @DecimalMin(value = "0.0001", message = "El valor unitario debe ser mayor a cero")
    private BigDecimal valorUnitario;

    private Boolean esCombustible;

    private String tipoCombustible;
}