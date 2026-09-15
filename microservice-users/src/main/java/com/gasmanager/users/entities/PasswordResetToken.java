package com.gasmanager.users.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fechaExpiracion = LocalDateTime.now().plusHours(24);

    @Column(nullable = false)
    private Boolean usado = false;

    public PasswordResetToken(String token, Usuario usuario){
        this.token = token;
        this.usuario = usuario;
        this.fechaExpiracion = LocalDateTime.now().plusHours(24);
    }
    public boolean estaExpirado(){
        return LocalDateTime.now().isAfter(fechaExpiracion);
    }
    public boolean esValido(){
        return !usado && !estaExpirado();
    }
}
