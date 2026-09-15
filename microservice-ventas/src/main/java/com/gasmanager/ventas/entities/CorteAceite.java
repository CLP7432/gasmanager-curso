package com.gasmanager.ventas.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "corte_aceites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorteAceite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "corte_id", nullable = false)
    private Long corteId;

    @Column(name = "surtidor_aceite_id", nullable = false)
    private Long surtidorAceiteId;

    @Column(name = "aceite_id", nullable = false)
    private Long aceiteId;

    @Column(name = "aceite_nombre", length = 120)
    private String aceiteNombre;

    @Column(name = "categoria", length = 30)
    private String categoria;

    @Column(name = "recibido_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal recibidoTotal;

    @Column(name = "sobrante", nullable = false, precision = 12, scale = 2)
    private BigDecimal sobrante;

    @Column(name = "vendidos", nullable = false, precision = 12, scale = 2)
    private BigDecimal vendidos;

    @Column(name = "precio_venta", precision = 12, scale = 2)
    private BigDecimal precioVenta;

    @Column(name = "importe", nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;
}
