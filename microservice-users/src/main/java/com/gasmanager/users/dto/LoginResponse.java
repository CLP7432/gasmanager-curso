package com.gasmanager.users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String rol;
    private Long idUsuario;
    private String correo;
    private List<String> permisos;
}
