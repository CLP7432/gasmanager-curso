package com.gasmanager.inventarios.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrecioHistoricoDTO {
    private Long id;
    private Long combustibleId;
    private String tipoCombustible;
    private BigDecimal precioAnterior;
    private BigDecimal precioNuevo;
    private LocalDateTime fechaCambio;
    private String motivoCambio;
    private String cambiadoPor;
    private Long cambiadoPorId;
}
