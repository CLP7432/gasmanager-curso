package com.gasmanager.ventas.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "surtidor_aceites_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurtidorAceiteItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "surtidor_id", nullable = false)
    private SurtidorAceite surtidor;

    @Column(name = "aceite_id", nullable = false)
    private Long aceiteId;

    @Column(name = "aceite_nombre", length = 120)
    private String aceiteNombre;

    @Column(name = "categoria", length = 30)
    private String categoria;

    @Column(name = "stock_actual", nullable = false, precision = 12, scale = 2)
    private BigDecimal stockActual;

    @Column(name = "stock_inicio_turno", precision = 12, scale = 2)
    private BigDecimal stockInicioTurno;

    @Column(name = "precio_venta", precision = 12, scale = 2)
    private BigDecimal precioVenta;
}
