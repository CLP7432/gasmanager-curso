package com.gasmanager.ventas.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class VentaDTO {

    private Long id;
    private String folio;
    private String fechaHora;
    private String metodoPago;
    private String estado;
    private Long turnoId;
    private Long despachadorId;
    private String despachadorNombre;
    private Long dispensarioId;
    private Long mangueraId;
    private Long usuarioId;
    private BigDecimal subtotal;
    private BigDecimal iva;
    private BigDecimal total;

    @Valid
    @NotEmpty(message = "La venta debe incluir al menos un producto")
    @Builder.Default
    private List<DetalleVentaDTO> detalles = new ArrayList<>();
}