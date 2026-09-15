package com.gasmanager.inventarios.entities;

import com.gasmanager.inventarios.enums.TipoCombustible;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tanques")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tanque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_combustible", nullable = false, length = 20)
    private TipoCombustible tipoCombustible;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combustible_id")
    private Combustible combustible;

    @Column(name = "capacidad_litros", nullable = false)
    private BigDecimal capacidadLitros;

    @Column(name = "stock_litros", nullable = false)
    private BigDecimal stockLitros = BigDecimal.ZERO;

    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

}