package com.gasmanager.ventas.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class NotaCreditoClienteDTO {
    private Long id;
    private String numero;
    private String clienteNombre;
    private LocalDate fechaCarga;
    private String origenCorte;
    private List<ItemNotaCreditoClienteDTO> items;

    @Data
    public static class ItemNotaCreditoClienteDTO {
        private String tipo;
        private String producto;
        private BigDecimal cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
    }
}
