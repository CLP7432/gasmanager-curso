package com.gasmanager.ventas.dto;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrarIncidenciaDTO {

    private Long empleadoId;
    private String tipo;
    private LocalDate fecha;
    private BigDecimal monto;
    private String observaciones;
    private String autorizadoPor;
}