package com.gasmanager.clientes.dto;

import com.gasmanager.clientes.enums.EstadoCredito;
import com.gasmanager.clientes.enums.MetodoPagoCredito;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditoDTO {

    private Long id;
    private String folioCredito;
    private Long clienteId;
    private String clienteNombre;

    private BigDecimal montoTotal;
    private BigDecimal montoPagado;
    private BigDecimal saldoPendiente;

    private Integer plazoMeses;
    private BigDecimal tasaInteres;
    private BigDecimal montoInteres;

    private LocalDate fechaInicio;
    private LocalDate fechaVencimiento;
    private LocalDate fechaUltimoPago;

    private EstadoCredito estado;
    private MetodoPagoCredito metodoPago;
    private Integer diaPago;
    private String notas;

    private List<AbonoCreditoDTO> abonos;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;



}
