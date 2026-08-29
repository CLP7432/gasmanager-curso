package com.gasmanager.users.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sesiones_usuarios")
@Getter
@Setter
@NoArgsConstructor
public class SesionUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "token", unique = true)
    private String token;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio = LocalDateTime.now();

    @Column(name = "ultima_actividad")
    private LocalDateTime ultimaActividad = LocalDateTime.now();

    @Column(name = "fecha_expiracion")
    private LocalDateTime fechaExpiracion;

    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "origen")
    private String origen;

    public SesionUsuario(Long idUsuario, String token){
        this.idUsuario = idUsuario;
        this.token = token;
        this.fechaInicio = LocalDateTime.now();
        this.activo = true;
    }

}
