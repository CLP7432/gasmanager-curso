package com.gasmanager.facturacion.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class VentaDTO {

    private Long id;
    private String folio;
    private String fechaHora;
    private String metodoPago;
    private String estado;
    private BigDecimal subtotal;
    private BigDecimal iva;
    private BigDecimal total;
    private List<DetalleVentaDTO> detalles;

    @Data
    @Builder
    public static class DetalleVentaDTO {
        private Long id;
        private String tipoProducto;
        private Long productoId;
        private String productoNombre;
        private BigDecimal cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
    }
}