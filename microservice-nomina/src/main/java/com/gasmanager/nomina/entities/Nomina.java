package com.gasmanager.nomina.entities;

import com.gasmanager.nomina.enums.EstadoNomina;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "nominas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Nomina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "folio_nomina", unique = true, nullable = false, length = 30)
    private String folioNomina;

    @Column(name = "periodo_inicio", nullable = false)
    private LocalDate periodoInicio;

    @Column(name = "periodo_fin", nullable = false)
    private LocalDate periodoFin;

    @Column(name = "fecha_pago")
    private LocalDate fechaPago;

    @Column(name = "fecha_procesamiento")
    private LocalDateTime fechaProcesamiento;

    @Column(name = "total_empleados")
    private Integer totalEmpleados = 0;

    @Column(name = "total_sueldos", precision = 14, scale = 2)
    private BigDecimal totalSueldos = BigDecimal.ZERO;

    @Column(name = "total_horas_extras", precision = 14, scale = 2)
    private BigDecimal totalHorasExtras = BigDecimal.ZERO;

    @Column(name = "total_bonos", precision = 14, scale = 2)
    private BigDecimal totalBonos = BigDecimal.ZERO;

    @Column(name = "total_deducciones", precision = 14, scale = 2)
    private BigDecimal totalDeducciones = BigDecimal.ZERO;

    @Column(name = "total_impuestos", precision = 14, scale = 2)
    private BigDecimal totalImpuestos = BigDecimal.ZERO;

    @Column(name = "total_neto", precision = 14, scale = 2)
    private BigDecimal totalNeto = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoNomina estado;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @OneToMany(mappedBy = "nomina", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<NominaDetalle> detalles = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void addDetalle(NominaDetalle detalle) {
        detalles.add(detalle);
        detalle.setNomina(this);
    }
}