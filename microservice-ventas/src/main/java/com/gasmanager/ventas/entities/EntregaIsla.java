package com.gasmanager.ventas.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "entregas_isla")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntregaIsla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "surtidor_aceite_id", nullable = false)
    private Long surtidorAceiteId;

    @Column(name = "aceite_id", nullable = false)
    private Long aceiteId;

    @Column(name = "aceite_nombre", length = 120)
    private String aceiteNombre;

    @Column(name = "cantidad", nullable = false, precision = 12, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "turno_id")
    private Long turnoId;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;
}
