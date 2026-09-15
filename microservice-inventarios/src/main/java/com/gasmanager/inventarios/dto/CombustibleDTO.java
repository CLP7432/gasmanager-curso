package com.gasmanager.inventarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CombustibleDTO {

    private Long id;

    @NotNull(message = "El tipo de combustible es obligatorio")
    private String tipo;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El precio actual es obligatorio")
    @Positive(message = "El precio actual debe ser mayor a 0")
    private BigDecimal precioActual;

    // Costo de compra (solo informativo, lo actualiza cada compra)
    private BigDecimal precioCompra;

    private LocalDateTime fechaUltimoCambioPrecio;

    private Boolean activo;

}
