package com.gasmanager.ventas.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearNotaCreditoRequestDTO {

    private Long creditoId;

    private String vehiculo;

    private String conductor;

    private LocalDate fechaCarga;

    private String origenCorte;

    private List<Item> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Item {
        private String tipo;
        private String producto;
        private BigDecimal cantidad;
        private String unidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
    }
}