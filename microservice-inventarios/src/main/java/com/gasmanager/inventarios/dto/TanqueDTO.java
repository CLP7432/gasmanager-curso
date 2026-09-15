package com.gasmanager.inventarios.dto;

import com.gasmanager.inventarios.enums.TipoCombustible;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TanqueDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotNull(message = "El combustible es obligatorio")
    private Long combustibleId;

    private TipoCombustible tipoCombustible;

    @NotNull(message = "La capacidad es obligatoria")
    @DecimalMin(value = "40000", message = "La capacidad debe ser al menos 40,000 litros para recibir una pipa de 30,000 litros")
    private BigDecimal capacidadLitros;

    @PositiveOrZero(message = "El stock no puede ser negativo")
    private BigDecimal stockLitros;

    private Boolean activo;
}