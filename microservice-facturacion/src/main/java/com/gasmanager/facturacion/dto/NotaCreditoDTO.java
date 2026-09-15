package com.gasmanager.facturacion.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class NotaCreditoDTO {

    private Long id;
    private String numero;
    private Long clienteId;
    private String clienteNombre;
    private BigDecimal saldo;
    private LocalDate fechaCarga;
    private String estado;
    private List<ItemNotaCreditoDTO> items;

    @Data
    @Builder
    public static class ItemNotaCreditoDTO {
        private String tipo;
        private String producto;
        private BigDecimal cantidad;
        private String unidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
    }
}