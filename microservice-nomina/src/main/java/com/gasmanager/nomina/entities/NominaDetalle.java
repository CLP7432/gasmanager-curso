package com.gasmanager.nomina.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "nominas_detalle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NominaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nomina_id", nullable = false)
    private Nomina nomina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @Column(name = "dias_trabajados", precision = 10, scale = 2)
    private BigDecimal diasTrabajados;

    @Column(name = "sueldo_base", precision = 12, scale = 2)
    private BigDecimal sueldoBase;

    @Column(name = "horas_extras", precision = 10, scale = 2)
    private BigDecimal horasExtras;

    @Column(name = "horas_extras_monto", precision = 12, scale = 2)
    private BigDecimal horasExtrasMonto;

    @Column(name = "faltas", precision = 10, scale = 2)
    private BigDecimal faltas;

    @Column(name = "faltas_descuento", precision = 12, scale = 2)
    private BigDecimal faltasDescuento;

    @Column(name = "retardo_descuento", precision = 12, scale = 2)
    private BigDecimal retardoDescuento;

    @Column(name = "permiso_sin_goce_descuento", precision = 12, scale = 2)
    private BigDecimal permisoSinGoceDescuento;

    @Column(name = "bonos", precision = 12, scale = 2)
    private BigDecimal bonos;

    @Column(name = "total_gravado", precision = 12, scale = 2)
    private BigDecimal totalGravado;

    @Column(name = "isr", precision = 12, scale = 2)
    private BigDecimal isr;

    @Column(name = "cuota_sindical", precision = 12, scale = 2)
    private BigDecimal cuotaSindical;

    @Column(name = "seguro_social", precision = 12, scale = 2)
    private BigDecimal seguroSocial;

    @Column(name = "infonavit", precision = 12, scale = 2)
    private BigDecimal infonavit;

    @Column(name = "otras_deducciones", precision = 12, scale = 2)
    private BigDecimal otrasDeducciones;

    // Sobrantes de caja: se suman al neto SIN impuestos
    @Column(name = "sobrantes_monto", precision = 12, scale = 2)
    private BigDecimal sobrantesMonto;

    @Column(name = "total_deducciones", precision = 12, scale = 2)
    private BigDecimal totalDeducciones;

    @Column(name = "neto_pagar", precision = 12, scale = 2)
    private BigDecimal netoPagar;
}