package com.gasmanager.ventas.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "caras_dispensario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaraDispensario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispensario_id", nullable = false)
    private Dispensario dispensario;

    @Column(name = "codigo", nullable = false, length = 5)
    private String codigo;

    @Column(name = "nombre", length = 50)
    private String nombre;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Builder.Default
    @OneToMany(mappedBy = "cara", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Manguera> mangueras = new ArrayList<>();
}