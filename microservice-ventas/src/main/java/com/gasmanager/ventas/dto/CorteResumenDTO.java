package com.gasmanager.ventas.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorteResumenDTO {

    private Long turnoId;
    private Long dispensarioId;
    private String dispensarioNombre;
    private String despachadorNombre;

    private Integer numeroVentas;
    private BigDecimal totalLitros;
    private BigDecimal totalVentas;

    private List<LineaResumenDTO> combustibles;
    private List<LineaResumenDTO> aceites;

    private List<CorteAceiteDTO> aceitesCorte;
    private BigDecimal aceiteImporte;

    private List<NotaCreditoCorteDTO> notasCredito;
    private BigDecimal notasCreditoTotal;

    private BigDecimal esperadoEfectivo;
    private BigDecimal esperadoTarjeta;
    private BigDecimal esperadoTransferencia;
    private BigDecimal esperadoCredito;

    private BigDecimal netoEfectivo;
}