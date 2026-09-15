package com.gasmanager.lealtad.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacciones_puntos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransaccionPuntos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "venta_id", nullable = false)
    private Long ventaId;

    @Column(name = "folio_venta", length = 20)
    private String folioVenta;

    @Column(name = "programa_id")
    private Long programaId;

    @Column(name = "programa_nombre", length = 120)
    private String programaNombre;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "monto")
    private BigDecimal monto;

    @Column(name = "litros")
    private BigDecimal litros;

    @Column(name = "puntos", nullable = false)
    private Integer puntos = 0;
}
