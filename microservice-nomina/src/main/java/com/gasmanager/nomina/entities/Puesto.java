package com.gasmanager.nomina.entities;

import com.gasmanager.nomina.enums.RiesgoPuesto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "puestos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Puesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 200)
    private String descripcion;

    @Column(name = "salario_base", precision = 12, scale = 2)
    private BigDecimal salarioBase;

    @Column(name = "salario_diario", precision = 12, scale = 2)
    private BigDecimal salarioDiario;

    @Enumerated(EnumType.STRING)
    @Column(name = "riesgo_puesto", length = 20)
    private RiesgoPuesto riesgoPuesto;

    @Column(name = "activo")
    private Boolean activo = true;

    @OneToMany(mappedBy = "puesto")
    @Builder.Default
    private List<Empleado> empleados = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}