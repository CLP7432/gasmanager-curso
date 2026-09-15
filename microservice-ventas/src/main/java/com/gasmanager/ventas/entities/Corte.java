package com.gasmanager.ventas.entities;

import com.gasmanager.ventas.enums.EstadoCorte;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cortes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Corte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_corte", unique = true, nullable = false, length = 20)
    private String codigoCorte;

    @Column(name = "turno_id", nullable = false)
    private Long turnoId;

    @Column(name = "dispensario_id", nullable = false)
    private Long dispensarioId;

    @Column(name = "dispensario_nombre", length = 120)
    private String dispensarioNombre;

    @Column(name = "despachador_id")
    private Long despachadorId;

    @Column(name = "despachador_nombre", length = 120)
    private String despachadorNombre;

    @Column(name = "numero_ventas", nullable = false)
    private Integer numeroVentas = 0;

    @Column(name = "total_litros", precision = 14, scale = 3)
    private BigDecimal totalLitros = BigDecimal.ZERO;

    @Column(name = "total_ventas", precision = 12, scale = 2)
    private BigDecimal totalVentas = BigDecimal.ZERO;

    @Column(name = "total_aceites", precision = 12, scale = 2)
    private BigDecimal totalAceites = BigDecimal.ZERO;

    @Column(name = "notas_credito_total", precision = 12, scale = 2)
    private BigDecimal notasCreditoTotal = BigDecimal.ZERO;

    @Column(name = "creditos_total", precision = 12, scale = 2)
    private BigDecimal creditosTotal = BigDecimal.ZERO;

    @Column(name = "esperado_efectivo", precision = 12, scale = 2)
    private BigDecimal esperadoEfectivo = BigDecimal.ZERO;

    @Column(name = "esperado_tarjeta", precision = 12, scale = 2)
    private BigDecimal esperadoTarjeta = BigDecimal.ZERO;

    @Column(name = "esperado_transferencia", precision = 12, scale = 2)
    private BigDecimal esperadoTransferencia = BigDecimal.ZERO;

    @Column(name = "esperado_credito", precision = 12, scale = 2)
    private BigDecimal esperadoCredito = BigDecimal.ZERO;

    @Column(name = "efectivo_recibido", precision = 12, scale = 2)
    private BigDecimal efectivoRecibido = BigDecimal.ZERO;

    @Column(name = "tarjeta_recibido", precision = 12, scale = 2)
    private BigDecimal tarjetaRecibido = BigDecimal.ZERO;

    @Column(name = "transferencia_recibido", precision = 12, scale = 2)
    private BigDecimal transferenciaRecibido = BigDecimal.ZERO;

    @Column(name = "diferencia_efectivo", precision = 12, scale = 2)
    private BigDecimal diferenciaEfectivo = BigDecimal.ZERO;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "notas_json", columnDefinition = "TEXT")
    private String notasJson;

    @Column(name = "creditos_json", columnDefinition = "TEXT")
    private String creditosJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoCorte estado;

    @Column(name = "validado_por", length = 120)
    private String validadoPor;

    @Column(name = "validado_fecha")
    private LocalDateTime validadoFecha;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }
}