package com.gasmanager.users.entities;

import com.gasmanager.users.enums.TipoAccion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditorias")
@Getter
@Setter
@NoArgsConstructor
public class AuditoriaAccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "id_usuario_ejecutor")
    private Long idUsuarioEjecutor;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_accion")
    private TipoAccion tipoAccion;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "modulo_afectado")
    private String moduloAfectado;

    private LocalDateTime fechaHora = LocalDateTime.now();

    @Lob
    @Column(name = "datos_anteriores")
    private String datosAnteriores;

    @Lob
    @Column(name = "datos_nuevos")
    private String datosNuevos;

    @Column(name = "origen")
    private String origen;
}
