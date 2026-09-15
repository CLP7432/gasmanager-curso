package com.gasmanager.ventas.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dispensarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dispensario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero", unique = true, nullable = false, length = 10)
    private String numero;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "ubicacion", length = 100)
    private String ubicacion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "despachador_id")
    private Long despachadorId;

    @Column(name = "despachador_nombre", length = 120)
    private String despachadorNombre;

    @Builder.Default
    @OneToMany(mappedBy = "dispensario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<CaraDispensario> caras = new ArrayList<>();
}