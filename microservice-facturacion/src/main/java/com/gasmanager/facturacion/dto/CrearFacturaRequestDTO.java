package com.gasmanager.facturacion.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CrearFacturaRequestDTO {

    @NotNull(message = "El cliente fiscal es obligatorio")
    private Long clienteFiscalId;

    private String formaPago;

    private String metodoPago;

    @Valid
    @NotEmpty(message = "La factura debe incluir al menos un concepto")
    private List<ConceptoFacturaRequestDTO> conceptos;
}