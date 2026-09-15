package com.gasmanager.clientes.dto;

import com.gasmanager.clientes.enums.EstadoNotaCredito;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotaCreditoDTO {

    private Long id;

    private String numero;

    @NotNull(message = "El crédito es obligatorio")
    private Long creditoId;

    private String creditoFolio;

    private String clienteNombre;

    private Long clienteId;

    private BigDecimal saldo;

    private String vehiculo;

    private String conductor;

    private LocalDate fechaCarga;

    private String origenCorte;

    private EstadoNotaCredito estado;

    private String notas;

    private List<ItemNotaCreditoDTO> items;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}