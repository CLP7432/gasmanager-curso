package com.gasmanager.ventas.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerarCorteDTO {

    private Long turnoId;
    private Long dispensarioId;
    private BigDecimal efectivoRecibido;
    private BigDecimal tarjetaRecibido;
    private BigDecimal transferenciaRecibido;
    private String observaciones;
    private List<GenerarCorteAceiteDTO> aceites;
    private List<NotaCreditoCorteDTO> notasCredito;
    private List<CreditoCorteDTO> creditos;
}