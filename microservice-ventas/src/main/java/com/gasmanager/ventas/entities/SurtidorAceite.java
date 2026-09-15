package com.gasmanager.ventas.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "surtidor_aceites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurtidorAceite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "despachador_id")
    private Long despachadorId;

    @Column(name = "despachador_nombre", length = 120)
    private String despachadorNombre;

    @Column(name = "dispensario_id")
    private Long dispensarioId;

    @Column(name = "nombre", length = 100)
    private String nombre;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Builder.Default
    @OneToMany(mappedBy = "surtidor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("id ASC")
    private List<SurtidorAceiteItem> items = new ArrayList<>();
}
