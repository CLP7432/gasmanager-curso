package com.gasmanager.inventarios.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "precios_historicos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrecioHistorico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combustible_id", nullable = false)
    private Combustible combustible;

    @Column(name = "precio_anterior", precision = 10, scale = 2)
    private BigDecimal precioAnterior;

    @Column(name = "precio_nuevo", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioNuevo;

    @Column(name = "fecha_cambio", nullable = false)
    private LocalDateTime fechaCambio;

    @Column(name = "motivo_cambio", length = 200)
    private String motivoCambio;

    @Column(name = "cambiado_por", length = 50)
    private String cambiadoPor;

    @Column(name = "cambiado_por_id")
    private Long cambiadoPorId;

    @PrePersist
    public void onCreate() {
        fechaCambio = LocalDateTime.now();
    }
}
