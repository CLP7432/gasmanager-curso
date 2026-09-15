package com.gasmanager.inventarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AceiteDTO {

    private Long id;

    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;
    private String marca;
    private String tipoAceite;
    private String presentacion;
    private Integer unidadesPorCaja;

    private BigDecimal precioCompra;

    private BigDecimal precioVenta;

    @PositiveOrZero
    private Integer stockActual;

    @PositiveOrZero
    private Integer stockMinimo;

    @PositiveOrZero
    private Integer stockMaximo;

    private String ubicacion;
    private Boolean activo;
}