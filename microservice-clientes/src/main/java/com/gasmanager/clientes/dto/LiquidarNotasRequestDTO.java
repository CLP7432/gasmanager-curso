package com.gasmanager.clientes.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiquidarNotasRequestDTO {

    @NotEmpty(message = "Debe seleccionar al menos una nota a liquidar")
    private List<Long> notaIds;

    private LocalDate fechaPago;

    private String metodoPago;

    private String referenciaPago;

    private String notas;
}