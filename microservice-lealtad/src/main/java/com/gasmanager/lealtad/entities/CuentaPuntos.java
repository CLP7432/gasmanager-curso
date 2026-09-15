package com.gasmanager.lealtad.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cuentas_puntos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaPuntos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "venta_id", unique = true, nullable = false)
    private Long ventaId;

    @Column(name = "folio_venta", length = 20)
    private String folioVenta;

    @Column(name = "litros")
    private BigDecimal litros;

    @Column(name = "puntos", nullable = false)
    private Integer puntos = 0;
}
