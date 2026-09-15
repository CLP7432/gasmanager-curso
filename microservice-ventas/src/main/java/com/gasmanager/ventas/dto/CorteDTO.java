package com.gasmanager.ventas.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorteDTO {

    private Long id;
    private String codigoCorte;
    private Long turnoId;
    private Long dispensarioId;
    private String dispensarioNombre;
    private Long despachadorId;
    private String despachadorNombre;
    private Integer numeroVentas;
    private BigDecimal totalLitros;
    private BigDecimal totalVentas;
    private BigDecimal totalAceites;
    private BigDecimal notasCreditoTotal;
    private BigDecimal creditosTotal;
    private BigDecimal esperadoEfectivo;
    private BigDecimal esperadoTarjeta;
    private BigDecimal esperadoTransferencia;
    private BigDecimal esperadoCredito;
    private BigDecimal efectivoRecibido;
    private BigDecimal tarjetaRecibido;
    private BigDecimal transferenciaRecibido;
    private BigDecimal diferenciaEfectivo;
    private String observaciones;
    private String estado;
    private String validadoPor;
    private String validadoFecha;
    private String createdAt;
    private List<CorteAceiteDTO> aceites;
    private List<NotaCreditoCorteDTO> notasCredito;
    private List<CreditoCorteDTO> creditos;
}