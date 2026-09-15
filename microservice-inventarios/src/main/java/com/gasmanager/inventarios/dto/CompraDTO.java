package com.gasmanager.inventarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class CompraDTO {

    private Long id;

    @NotBlank(message = "El folio de factura es obligatorio")
    private String folioFactura;

    @NotNull(message = "El proveedor es obligatorio")
    private Long proveedorId;

    private String proveedorRazonSocial;
    private String proveedorRfc;

    private String fechaFactura;
    private String fechaRegistro;
    private String tipoCompra;
    private BigDecimal subtotal;
    private BigDecimal iva;
    private BigDecimal total;
    private Boolean activo;
    private String estadoDescarga;

    @Builder.Default
    private List<CompraDetalleDTO> detalles = new ArrayList<>();
}