package com.gasmanager.lealtad.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "programas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramaLealtad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "descripcion", length = 300)
    private String descripcion;

    // Puntos por cada litro de combustible
    @Column(name = "puntos_por_litro", nullable = false)
    private Integer puntosPorLitro;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    // Solo un programa puede estar activo a la vez
    @Column(name = "activo", nullable = false)
    private Boolean activo = false;
}
