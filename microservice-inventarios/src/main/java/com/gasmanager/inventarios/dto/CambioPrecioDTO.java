package com.gasmanager.inventarios.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CambioPrecioDTO {
    @NotNull(message = "El nuevo precio es obligatorio")
    @Positive(message = "El nuevo precio debe ser mayor a 0")
    private BigDecimal nuevoPrecio;

    private String motivo;
}
