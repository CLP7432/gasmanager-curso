package com.gasmanager.inventarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CompraDetalleDTO {

    private Long id;

    @NotBlank(message = "El tipo de producto es obligatorio")
    private String tipoProducto;

    private Long aceiteId;
    private Long combustibleId;
    private String productoNombre;
    private String presentacion;
    private Integer unidadesPorCaja;
    private Integer cajas;
    private Integer piezas;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private BigDecimal cantidad;

    @NotNull(message = "El precio unitario es obligatorio")
    @Positive(message = "El precio unitario debe ser mayor a 0")
    private BigDecimal precioUnitario;

    private BigDecimal subtotal;

    private BigDecimal litrosDescargados;
}