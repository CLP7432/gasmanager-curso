package com.gasmanager.clientes.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbonoCreditoDTO {

    private Long id;
    private String folioAbono;
    private Long creditoId;
    private String creditoFolio;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal monto;

    @NotNull
    private LocalDate fechaAbono;

    @NotBlank
    private String metodoPago;

    private String referenciaPago;
    private String notas;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
