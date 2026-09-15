package com.gasmanager.inventarios.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DescargaPipaDTO {

    @NotNull(message = "El detalle de la compra es obligatorio")
    private Long detalleId;

    @NotNull(message = "Los litros a descargar son obligatorios")
    @Positive(message = "Los litros a descargar deben ser mayores a 0")
    private BigDecimal litros;
}